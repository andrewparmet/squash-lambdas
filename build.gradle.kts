import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.shadow)
    alias(libs.plugins.spotless)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(libs.aws.bom))
    implementation(platform(libs.log4j.bom))

    implementation(kotlin("reflect"))
    implementation(libs.aws.dynamodb)
    implementation(libs.aws.lambda)
    implementation(libs.aws.s3)
    implementation(libs.aws.ses)
    implementation(libs.aws.sns)
    implementation(libs.biweekly)
    implementation(libs.commons.configuration)
    implementation(libs.commons.email)
    implementation(libs.google.calendar)
    implementation(libs.google.oauth2.http)
    implementation(libs.gson)
    implementation(libs.gson.extras)
    implementation(libs.gson.javatime)
    implementation(libs.guava)
    implementation(libs.jsoup)
    implementation(libs.kotlinLogging)
    implementation(libs.log4j.core)
    implementation(libs.opencsv)

    runtimeOnly(libs.log4j.jcl)
    runtimeOnly(libs.log4j.slf4jImpl)

    testImplementation(libs.junit)
    testImplementation(libs.reflections)
    testImplementation(libs.truth)
}

spotless {
    kotlin {
        ktlint()
            .editorConfigOverride(
                mapOf(
                    "max_line_length" to 120,
                    "ktlint_ignore_back_ticked_identifier" to "true",
                    "ktlint_class_signature_rule_force_multiline_when_parameter_count_greater_or_equal_than" to 1,
                    "ktlint_function_signature_body_expression_wrapping" to "always",
                    "ktlint_standard_trailing-comma-on-call-site" to "disabled",
                    "ktlint_standard_trailing-comma-on-declaration-site" to "disabled",
                    "ktlint_standard_discouraged-comment-location" to "disabled",
                    "ij_kotlin_packages_to_use_import_on_demand" to null,
                )
            )
    }
}

tasks.withType<KotlinCompile>().all {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
        allWarningsAsErrors.set(true)
    }
    dependsOn("generateGitShaConstant")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val gitShaOutputDir = file("${layout.buildDirectory.get()}/generated/git-sha")

sourceSets["main"].java.srcDir(gitShaOutputDir)

tasks.register("generateGitShaConstant") {
    doFirst {
        val srcFile = File(gitShaOutputDir, "com/parmet/squashlambdas/GitSha.kt")
        srcFile.parentFile.mkdirs()
        srcFile.writeText(
            """
                package com.parmet.squashlambdas
                
                const val GIT_SHA = "${getGitSha()}"
                
            """.trimIndent()
        )
    }
}

fun getGitSha(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
        standardOutput = stdout
    }
    return stdout.toString().trim()
}
