package com.parmet.squashlambdas.infra

import aws.smithy.kotlin.runtime.SdkBaseException
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import kotlinx.coroutines.delay
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

private const val MAX_ATTEMPTS = 180
private const val POLL_INTERVAL_MILLIS = 5_000L

private class WaitForSnapStart : SuspendingCliktCommand(name = "wait-for-snapstart") {
    override suspend fun run() {
        val region = requireEnvironment("AWS_REGION")
        val profile = System.getenv("AWS_PROFILE") ?: "personal"
        val functionLabel = requireEnvironment("FUNCTION_LABEL")
        val functionName = requireEnvironment("FUNCTION_NAME")
        val functionVersion = requireEnvironment("FUNCTION_VERSION")
        val diagnostics = Path.of(requireEnvironment("SNAPSTART_DIAGNOSTICS_FILE"))
        val aws = Aws(profile, region)

        try {
            repeat(MAX_ATTEMPTS) {
                val status =
                    try {
                        aws.snapshotStatus(functionName, functionVersion)
                    } catch (_: SdkBaseException) {
                        delay(POLL_INTERVAL_MILLIS)
                        return@repeat
                    }
                if (status.state == "Active" && status.optimization == "On") {
                    return
                }
                if (status.state == "Failed") {
                    diagnostics.append(
                        "$functionLabel: snapshot failed with ${status.stateReasonCode ?: "an unknown reason"}"
                    )
                    error("Lambda snapshot failed")
                }

                delay(POLL_INTERVAL_MILLIS)
            }
        } finally {
            aws.close()
        }

        diagnostics.append("$functionLabel: timed out waiting for the Lambda snapshot")
        error("Timed out waiting for SnapStart")
    }
}

private fun requireEnvironment(name: String): String =
    requireNotNull(System.getenv(name)) { "$name is required" }

private fun Path.append(message: String) {
    Files.writeString(this, "$message\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND)
}

suspend fun main(args: Array<String>) =
    WaitForSnapStart().main(args)
