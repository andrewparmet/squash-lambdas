package com.parmet.squashlambdas.infra

import java.nio.file.Path

internal class CommandRunner(private val workingDirectory: Path) {
    fun awsLogin(profile: String, region: String) {
        interactive(listOf("aws", "login", "--profile", profile, "--region", region))
    }

    fun interactive(command: List<String>, environment: Map<String, String> = emptyMap()) {
        check(interactiveResult(command, environment) == 0) {
            "Command failed: ${command.take(2).joinToString(" ")}"
        }
    }

    fun interactiveResult(command: List<String>, environment: Map<String, String> = emptyMap()): Int {
        val builder = ProcessBuilder(command).directory(workingDirectory.toFile()).inheritIO()
        builder.environment().putAll(environment)
        return builder.start().waitFor()
    }
}
