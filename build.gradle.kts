import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream

plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.jetbrains.kotlin.jvm") version "1.8.0"
    id("org.jlleitschuh.gradle.ktlint") version "11.1.0"
}

repositories {
    mavenCentral()
}

val awsSdkVersion = "1.11.676"
val log4jVersion = "2.12.1"

dependencies {
    implementation("com.amazonaws:aws-java-sdk-dynamodb:$awsSdkVersion")
    implementation("com.amazonaws:aws-java-sdk-s3:$awsSdkVersion")
    implementation("com.amazonaws:aws-java-sdk-ses:$awsSdkVersion")
    implementation("com.amazonaws:aws-java-sdk-sns:$awsSdkVersion")
    implementation("com.amazonaws:aws-lambda-java-core:1.2.0")

    implementation("com.fatboyindustrial.gson-javatime-serialisers:gson-javatime-serialisers:1.1.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.danilopianini:gson-extras:1.2.0")
    implementation("com.google.guava:guava:31.1-jre")
    implementation("com.google.auth:google-auth-library-oauth2-http:0.17.1")
    implementation("com.google.apis:google-api-services-calendar:v3-rev20190910-1.30.1")

    implementation("net.sf.biweekly:biweekly:0.6.3")
    implementation("org.apache.commons:commons-email:1.5")
    implementation("org.jsoup:jsoup:1.12.1")
    implementation("org.apache.commons:commons-configuration2:2.6")
    implementation("com.opencsv:opencsv:5.0")

    implementation(kotlin("reflect"))
    implementation("io.github.microutils:kotlin-logging:1.7.6")

    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-jcl:$log4jVersion")

    testImplementation("junit:junit:4.12")
    testImplementation("com.google.truth:truth:1.0")
    testImplementation("org.reflections:reflections:0.9.11")
}

tasks.register("stage") {
    dependsOn("ktlintCheck", "shadowJar")
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = "11"
        allWarningsAsErrors = true
    }
    dependsOn("generateGitShaConstant")
}

val gitShaOutputDir = file("$buildDir/generated-sources/git-sha")

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
