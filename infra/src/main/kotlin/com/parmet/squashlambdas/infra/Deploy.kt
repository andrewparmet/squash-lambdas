package com.parmet.squashlambdas.infra

import aws.smithy.kotlin.runtime.SdkBaseException
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.path
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.readText

private const val MAX_DIRECT_UPLOAD_BYTES = 50L * 1024L * 1024L
private const val DEFAULT_VERSIONS_TO_KEEP = 5

private class Deploy : SuspendingCliktCommand(name = "deploy") {
    private val repositoryDirectory by argument().path(mustExist = true, canBeFile = false)

    override suspend fun run() {
        InfrastructurePublisher(repositoryDirectory).publish()
    }
}

internal class InfrastructurePublisher(private val repositoryDirectory: Path) {
    private val profile = System.getenv("AWS_PROFILE") ?: "personal"
    private val region = System.getenv("TF_VAR_aws_region") ?: "us-east-1"
    private val infrastructureDirectory = repositoryDirectory.resolve("infra")
    private val artifact = repositoryDirectory.resolve("app/build/libs/squash-lambdas-all.jar")
    private val diagnostics = infrastructureDirectory.resolve("build/snapstart-diagnostics.log")
    private val commands = CommandRunner(repositoryDirectory)
    private val aws = Aws(profile, region)
    private val bootstrap = Bootstrap(aws)

    suspend fun publish() =
        publish(authenticate = true)

    suspend fun publishWithExistingLogin() =
        publish(authenticate = false)

    private suspend fun publish(authenticate: Boolean) =
        try {
            if (authenticate) {
                commands.awsLogin(profile, region)
            }
            publishConfiguration()
        } finally {
            aws.close()
        }

    private suspend fun publishConfiguration() {
        check(artifact.exists()) { "Build artifact is missing" }
        check(artifact.fileSize() <= MAX_DIRECT_UPLOAD_BYTES) {
            "Build artifact exceeds Lambda's 50 MB direct-upload limit"
        }
        val configuration = bootstrap.load().value
        val resourceNames = configuration.requiredObject("resource_names")
        val privateConfig = configuration.requiredObject("private_config")
        val backend = configuration.requiredObject("backend")
        val terraformEnvironment =
            mapOf(
                "AWS_PROFILE" to profile,
                "TF_VAR_aws_region" to region,
                "TF_VAR_resource_names" to resourceNames.toString(),
                "TF_VAR_private_config" to privateConfig.toString()
            )

        try {
            Files.createDirectories(diagnostics.parent)
            Files.writeString(diagnostics, "")
            commands.interactive(
                listOf(
                    "terraform",
                    "-chdir=${infrastructureDirectory.absolutePathString()}",
                    "init",
                    "-reconfigure",
                    "-backend-config=bucket=${backend.requiredString("bucket")}",
                    "-backend-config=key=${backend.requiredString("key")}",
                    "-backend-config=region=$region",
                    "-backend-config=encrypt=true",
                    "-backend-config=use_lockfile=true",
                    "-backend-config=profile=$profile"
                ),
                terraformEnvironment
            )
            val applyResult =
                commands.interactiveResult(
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
            try {
                pruneVersions(resourceNames.requiredObject("functions"))
            } catch (failure: SdkBaseException) {
                System.err.println("Lambda version pruning failed after a successful deployment: ${failure.message}")
            }
        } finally {
            Files.deleteIfExists(diagnostics)
        }
    }

    private suspend fun pruneVersions(functions: JsonObject) {
        val keepCount = System.getenv("LAMBDA_VERSIONS_TO_KEEP")?.toIntOrNull() ?: DEFAULT_VERSIONS_TO_KEEP
        require(keepCount > 0) { "LAMBDA_VERSIONS_TO_KEEP must be a positive integer" }
        var deletedCount = 0

        val functionNames =
            functions.requiredObject("email_parsers").values.map { it.jsonPrimitive.content } +
                functions.requiredString("monitor") +
                functions.requiredString("reservation")
        functionNames.forEach { functionName ->
            val aliasedVersions = aws.aliases(functionName)
            val versions = aws.versions(functionName)

            versionsToDelete(versions, aliasedVersions, keepCount).forEach { version ->
                aws.deleteVersion(functionName, version)
                deletedCount++
            }
        }

        println("Removed $deletedCount unaliased Lambda versions")
    }
}

internal fun versionsToDelete(versions: List<Int>, aliasedVersions: Set<Int>, keepCount: Int): List<Int> {
    val newest = versions.sortedDescending().take(keepCount).toSet()
    return versions.sorted().filterNot { it in newest || it in aliasedVersions }
}

suspend fun main(args: Array<String>) =
    Deploy().main(args)
