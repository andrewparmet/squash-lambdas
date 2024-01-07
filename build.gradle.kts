import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("com.diffplug.spotless") version "6.23.3"
}

repositories {
    mavenCentral()
}

val awsSdkVersion = "1.12.632"
val log4jVersion = "2.22.0"

dependencies {
    implementation("com.amazonaws:aws-java-sdk-dynamodb:$awsSdkVersion")
    implementation("com.amazonaws:aws-java-sdk-s3:$awsSdkVersion")
    implementation("com.amazonaws:aws-java-sdk-ses:$awsSdkVersion")
    implementation("com.amazonaws:aws-java-sdk-sns:$awsSdkVersion")
    implementation("com.amazonaws:aws-lambda-java-core:1.2.3")

    implementation("com.fatboyindustrial.gson-javatime-serialisers:gson-javatime-serialisers:1.1.2")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.danilopianini:gson-extras:1.2.0")
    implementation("com.google.guava:guava:33.0.0-jre")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.16.0")
    implementation("com.google.apis:google-api-services-calendar:v3-rev20231123-2.0.0")

    implementation("net.sf.biweekly:biweekly:0.6.7")
    implementation("org.apache.commons:commons-email:1.5")
    implementation("org.jsoup:jsoup:1.15.4")
    implementation("org.apache.commons:commons-configuration2:2.8.0")
    implementation("com.opencsv:opencsv:5.9")

    implementation(kotlin("reflect"))
    implementation("io.github.microutils:kotlin-logging:3.0.5")

    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-jcl:$log4jVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("org.reflections:reflections:0.10.2")
}

spotless {
    kotlin {
        ktlint()
            .editorConfigOverride(
                mapOf(
                    "ktlint_standard_trailing-comma-on-call-site" to "disabled",
                    "ktlint_standard_trailing-comma-on-declaration-site" to "disabled",
                    "ij_kotlin_packages_to_use_import_on_demand" to null
                )
            )
    }
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = "11"
        allWarningsAsErrors = true
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
