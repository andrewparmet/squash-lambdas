@file:DependsOn("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.PosixFilePermissions
import java.time.LocalDate
import java.time.ZoneId
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.readText
import kotlin.system.exitProcess

val maxDirectUploadBytes = 50L * 1024L * 1024L
val defaultVersionsToKeep = 5
val maxSnapStartAttempts = 180
val snapStartPollIntervalMillis = 5_000L

class InfrastructurePublisher(private val repositoryDirectory: Path) {
    private val profile = System.getenv("AWS_PROFILE") ?: "personal"
    private val region = System.getenv("TF_VAR_aws_region") ?: "us-east-1"
    private val infrastructureDirectory = repositoryDirectory.resolve("infra")
    private val artifact = repositoryDirectory.resolve("build/libs/squash-lambdas-all.jar")
    private val diagnostics = repositoryDirectory.resolve("build/snapstart-diagnostics.log")
    private val errorLog = repositoryDirectory.resolve("build/deployment-errors.log")

    fun publish() {
        check(runCaptured(listOf("git", "status", "--porcelain")).isBlank()) {
            "Refusing to publish from a dirty working tree"
        }
        runInteractive(
            listOf(
                repositoryDirectory.resolve("gradlew").absolutePathString(),
                "clean",
                "spotlessCheck",
                "test",
                "shadowJar"
            )
        )
        check(artifact.exists()) { "Build artifact is missing" }
        check(artifact.fileSize() <= maxDirectUploadBytes) {
            "Build artifact exceeds Lambda's 50 MB direct-upload limit"
        }
        Files.deleteIfExists(errorLog)

        runInteractive(listOf("aws", "login", "--profile", profile, "--region", region))
        val bootstrap = loadBootstrap()
        val resourceNames = bootstrap.requiredObject("resource_names")
        val privateConfig = bootstrap.requiredObject("private_config")
        val backend = bootstrap.requiredObject("backend")
        val terraformEnvironment =
            mapOf(
                "AWS_PROFILE" to profile,
                "TF_VAR_aws_region" to region,
                "TF_VAR_resource_names" to resourceNames.toString(),
                "TF_VAR_private_config" to privateConfig.toString()
            )

        val backendConfiguration = Files.createTempFile("squash-lambdas-backend-", ".hcl")
        try {
            Files.setPosixFilePermissions(backendConfiguration, PosixFilePermissions.fromString("rw-------"))
            Files.writeString(backendConfiguration, backendConfiguration(backend))
            Files.writeString(diagnostics, "")
            runInteractive(
                listOf(
                    "terraform",
                    "-chdir=${infrastructureDirectory.absolutePathString()}",
                    "init",
                    "-reconfigure",
                    "-backend-config=${backendConfiguration.absolutePathString()}"
                ),
                terraformEnvironment
            )
            val applyResult =
                runInteractiveResult(
                    listOf("terraform", "-chdir=${infrastructureDirectory.absolutePathString()}", "apply"),
                    terraformEnvironment
                )
            if (applyResult != 0) {
                if (diagnostics.exists() && diagnostics.fileSize() > 0) {
                    System.err.println("SnapStart diagnostics:")
                    diagnostics.readText().lineSequence().filter { it.isNotBlank() }.forEach {
                        System.err.println("  $it")
                    }
                }
                error("Terraform apply failed")
            }
            runCatching { pruneVersions(resourceNames.requiredObject("functions")) }.onFailure {
                System.err.println("Lambda version pruning failed after a successful deployment: ${it.message}")
            }
        } finally {
            Files.deleteIfExists(backendConfiguration)
            Files.deleteIfExists(diagnostics)
        }
    }

    fun cleanup() {
        Files.deleteIfExists(errorLog)
        runInteractive(listOf("aws", "login", "--profile", profile, "--region", region))
        val resourceNames = loadBootstrap().requiredObject("resource_names")
        deleteExpiredSnapshots(resourceNames.requiredString("table"))
    }

    private fun loadBootstrap(): JsonObject {
        val arns =
            Json.parseToJsonElement(
                runCaptured(
                    listOf(
                        "aws",
                        "resourcegroupstaggingapi",
                        "get-resources",
                        "--profile",
                        profile,
                        "--region",
                        region,
                        "--resource-type-filters",
                        "ssm:parameter",
                        "--tag-filters",
                        "Key=Application,Values=squash-lambdas",
                        "Key=Role,Values=terraform-bootstrap",
                        "--query",
                        "ResourceTagMappingList[].ResourceARN",
                        "--output",
                        "json"
                    )
                )
            ).jsonArray
        check(arns.size == 1) { "Expected exactly one Terraform bootstrap parameter, found ${arns.size}" }

        val suffix = arns.single().jsonPrimitive.content.substringAfter(":parameter/")
        val value =
            getParameter(suffix) ?: getParameter("/$suffix")
                ?: error("Unable to read the Terraform bootstrap parameter")
        return parseObject(value, "bootstrap parameter")
    }

    private fun getParameter(name: String): String? = runCapturedOrNull(
        listOf(
            "aws",
            "ssm",
            "get-parameter",
            "--profile",
            profile,
            "--region",
            region,
            "--name",
            name,
            "--with-decryption",
            "--query",
            "Parameter.Value",
            "--output",
            "text"
        )
    )

    private fun backendConfiguration(backend: JsonObject): String =
        """
        bucket = "${backend.requiredString("bucket").hclEscape()}"
        key = "${backend.requiredString("key").hclEscape()}"
        region = "${region.hclEscape()}"
        encrypt = true
        use_lockfile = true
        profile = "${profile.hclEscape()}"
        """.trimIndent() + "\n"

    private fun pruneVersions(functions: JsonObject) {
        val keepCount = System.getenv("LAMBDA_VERSIONS_TO_KEEP")?.toIntOrNull() ?: defaultVersionsToKeep
        require(keepCount > 0) { "LAMBDA_VERSIONS_TO_KEEP must be a positive integer" }
        var deletedCount = 0

        functions.values.map { it.jsonPrimitive.content }.forEach { functionName ->
            val aliases =
                parseObject(
                    runCaptured(awsLambdaCommand(functionName, "list-aliases") + listOf("--output", "json")),
                    "Lambda aliases"
                )
            val aliasedVersions =
                aliases.getValue("Aliases").jsonArray.flatMap { aliasElement ->
                    val alias = aliasElement.jsonObject
                    buildList {
                        alias["FunctionVersion"]?.jsonPrimitive?.contentOrNull?.toIntOrNull()?.let(::add)
                        alias["RoutingConfig"]?.jsonObject?.get("AdditionalVersionWeights")?.jsonObject?.keys
                            ?.mapNotNull(String::toIntOrNull)
                            ?.let(::addAll)
                    }
                }.toSet()
            val versions =
                parseStrings(
                    runCaptured(
                        awsLambdaCommand(functionName, "list-versions-by-function") +
                            listOf("--query", "Versions[?Version!=`${'$'}LATEST`].Version", "--output", "json")
                    )
                ).map(String::toInt)

            versionsToDelete(versions, aliasedVersions, keepCount).forEach { version ->
                runCaptured(
                    awsLambdaCommand(functionName, "delete-function") + listOf("--qualifier", version.toString())
                )
                deletedCount++
            }
        }

        println("Removed $deletedCount unaliased Lambda versions")
    }

    private fun deleteExpiredSnapshots(tableName: String) {
        val today = LocalDate.now(ZoneId.of("America/New_York"))
        var exclusiveStartKey: JsonObject? = null
        var deletedCount = 0

        do {
            val command =
                mutableListOf(
                    "aws",
                    "dynamodb",
                    "scan",
                    "--profile",
                    profile,
                    "--region",
                    region,
                    "--table-name",
                    tableName,
                    "--projection-expression",
                    "filename,#ttl",
                    "--expression-attribute-names",
                    "{\"#ttl\":\"ttl\"}",
                    "--output",
                    "json"
                )
            exclusiveStartKey?.let {
                command += listOf("--exclusive-start-key", it.toString())
            }
            val response = parseObject(runCaptured(command), "DynamoDB scan response")
            response.getValue("Items").jsonArray.map { it.jsonObject }.filter { "ttl" !in it }.mapNotNull { item ->
                item.requiredObject("filename").requiredString("S").takeIf { isExpiredSnapshot(it, today) }
            }.forEach { filename ->
                val key =
                    buildJsonObject {
                        put("filename", buildJsonObject { put("S", filename) })
                    }
                val deleteCommand =
                    listOf(
                        "aws",
                        "dynamodb",
                        "delete-item",
                        "--profile",
                        profile,
                        "--region",
                        region,
                        "--table-name",
                        tableName,
                        "--key",
                        key.toString(),
                        "--condition-expression",
                        "attribute_not_exists(#ttl)",
                        "--expression-attribute-names",
                        "{\"#ttl\":\"ttl\"}"
                    )
                val result = runCapturedResult(deleteCommand)
                if (result.exitCode == 0) {
                    deletedCount++
                } else if (!result.output.contains("ConditionalCheckFailedException")) {
                    failCommand(deleteCommand, result)
                }
            }
            exclusiveStartKey = response["LastEvaluatedKey"]?.jsonObject?.takeIf { it.isNotEmpty() }
        } while (exclusiveStartKey != null)

        println("Removed $deletedCount stale slot snapshots without TTL")
    }

    private fun awsLambdaCommand(functionName: String, operation: String): List<String> = listOf(
        "aws",
        "lambda",
        operation,
        "--profile",
        profile,
        "--region",
        region,
        "--function-name",
        functionName
    )

    private fun runCaptured(command: List<String>): String = runCapturedResult(command).let { result ->
        if (result.exitCode != 0) {
            failCommand(command, result)
        }
        result.output.trim()
    }

    private fun failCommand(command: List<String>, result: CommandResult): Nothing {
        Files.writeString(errorLog, result.output, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        Files.setPosixFilePermissions(errorLog, PosixFilePermissions.fromString("rw-------"))
        error("Command failed: ${command.take(3).joinToString(" ")}. Details: ${errorLog.absolutePathString()}")
    }

    private fun runCapturedOrNull(command: List<String>): String? =
        runCapturedResult(command).takeIf { it.exitCode == 0 }?.output?.trim()

    private fun runCapturedResult(command: List<String>): CommandResult {
        val process =
            ProcessBuilder(command)
                .directory(repositoryDirectory.toFile())
                .redirectErrorStream(true)
                .start()
        val output = process.inputStream.bufferedReader().use { it.readText() }
        return CommandResult(process.waitFor(), output)
    }

    private fun runInteractive(command: List<String>, environment: Map<String, String> = emptyMap()) {
        check(runInteractiveResult(command, environment) == 0) {
            "Command failed: ${command.take(2).joinToString(" ")}"
        }
    }

    private fun runInteractiveResult(command: List<String>, environment: Map<String, String> = emptyMap()): Int {
        val builder = ProcessBuilder(command).directory(repositoryDirectory.toFile()).inheritIO()
        builder.environment().putAll(environment)
        return builder.start().waitFor()
    }
}

data class CommandResult(val exitCode: Int, val output: String)

fun waitForSnapStart() {
    val region = requireEnvironment("AWS_REGION")
    val functionLabel = requireEnvironment("FUNCTION_LABEL")
    val functionName = requireEnvironment("FUNCTION_NAME")
    val functionVersion = requireEnvironment("FUNCTION_VERSION")
    val diagnostics = Path.of(requireEnvironment("SNAPSTART_DIAGNOSTICS_FILE"))

    var configurationRead = false
    repeat(maxSnapStartAttempts) {
        val result = lambdaConfiguration(region, functionName, functionVersion)
        if (result == null) {
            Thread.sleep(snapStartPollIntervalMillis)
            return@repeat
        }
        configurationRead = true

        val fields = result.split(Regex("\\s+"))
        val state = fields.getOrNull(0)
        val stateReasonCode = fields.getOrNull(1)
        val optimization = fields.getOrNull(2)
        if (state == "Active" && optimization == "On") {
            return
        }
        if (state == "Failed") {
            diagnostics.append("$functionLabel: snapshot failed with ${stateReasonCode ?: "an unknown reason"}")
            error("Lambda snapshot failed")
        }

        Thread.sleep(snapStartPollIntervalMillis)
    }

    val message =
        if (configurationRead) {
            "timed out waiting for the Lambda snapshot"
        } else {
            "unable to read Lambda configuration"
        }
    diagnostics.append("$functionLabel: $message")
    error("Timed out waiting for SnapStart")
}

fun lambdaConfiguration(region: String, functionName: String, functionVersion: String): String? {
    val process =
        ProcessBuilder(
            "aws",
            "lambda",
            "get-function-configuration",
            "--function-name",
            "$functionName:$functionVersion",
            "--region",
            region,
            "--query",
            "[State,StateReasonCode,SnapStart.OptimizationStatus]",
            "--output",
            "text"
        ).redirectError(ProcessBuilder.Redirect.DISCARD).start()
    val output = process.inputStream.bufferedReader().use { it.readText() }
    return output.trim().takeIf { process.waitFor() == 0 }
}

fun versionsToDelete(versions: List<Int>, aliasedVersions: Set<Int>, keepCount: Int): List<Int> {
    val newest = versions.sortedDescending().take(keepCount).toSet()
    return versions.sorted().filterNot { it in newest || it in aliasedVersions }
}

fun isExpiredSnapshot(filename: String, today: LocalDate): Boolean {
    val match = Regex("^(\\d{4}-\\d{2}-\\d{2})/taken$").matchEntire(filename) ?: return false
    val date = runCatching { LocalDate.parse(match.groupValues[1]) }.getOrNull() ?: return false
    return date < today
}

fun parseStrings(value: String): List<String> = Json.parseToJsonElement(value).let { element ->
    require(element is JsonArray)
    element.map { it.jsonPrimitive.content }
}

fun parseObject(value: String, description: String): JsonObject =
    runCatching { Json.parseToJsonElement(value).jsonObject }.getOrElse {
        error("Invalid $description JSON")
    }

fun JsonObject.requiredObject(name: String): JsonObject = getValue(name).jsonObject

fun JsonObject.requiredString(name: String): String = getValue(name).jsonPrimitive.content

fun String.hclEscape(): String = replace("\\", "\\\\").replace("\"", "\\\"")

fun requireEnvironment(name: String): String = requireNotNull(System.getenv(name)) { "$name is required" }

fun Path.append(message: String) {
    Files.writeString(this, "$message\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND)
}

try {
    when (args.firstOrNull()) {
        "publish" -> {
            require(args.size == 2) { "Usage: deploy.main.kts publish REPOSITORY_DIRECTORY" }
            InfrastructurePublisher(Path.of(args[1])).publish()
        }

        "wait-for-snapstart" -> {
            require(args.size == 1) { "Usage: deploy.main.kts wait-for-snapstart" }
            waitForSnapStart()
        }

        "cleanup" -> {
            require(args.size == 2) { "Usage: deploy.main.kts cleanup REPOSITORY_DIRECTORY" }
            InfrastructurePublisher(Path.of(args[1])).cleanup()
        }

        "self-test" -> {
            require(versionsToDelete((1..10).toList(), setOf(2), 5) == listOf(1, 3, 4, 5))
            val today = LocalDate.of(2026, 9, 5)
            require(isExpiredSnapshot("2026-09-04/taken", today))
            require(!isExpiredSnapshot("2026-09-05/taken", today))
            require(!isExpiredSnapshot("unrelated", today))
        }

        else -> error("Expected publish, cleanup, self-test, or wait-for-snapstart")
    }
} catch (failure: Exception) {
    System.err.println(failure.message ?: failure::class.simpleName)
    exitProcess(1)
}
