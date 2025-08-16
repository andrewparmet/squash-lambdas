import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.buildConfig)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.shadow)
    alias(libs.plugins.spotless)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(libs.aws2.bom))
    implementation(platform(libs.log4j.bom))

    implementation(kotlin("reflect"))
    implementation(libs.aws2.sdk.core)
    implementation(libs.aws2.dynamodb)
    implementation(libs.aws.lambda)
    implementation(libs.aws2.s3)
    implementation(libs.aws2.sns)
    implementation(libs.biweekly)
    implementation(libs.commons.email)
    implementation(libs.google.calendar)
    implementation(libs.google.oauth2.http)
    implementation(libs.gson)
    implementation(libs.gson.extras)
    implementation(libs.gson.javatime)
    implementation(libs.guava)
    implementation(libs.guice)
    implementation(libs.hoplite)
    implementation(libs.jsoup)
    implementation(libs.kotlinLogging)
    implementation(libs.log4j.core)
    implementation(libs.opencsv)

    runtimeOnly(libs.log4j.jcl)
    runtimeOnly(libs.log4j.slf4jImpl)

    testImplementation(libs.junit)
    testImplementation(libs.reflections)
    testImplementation(libs.truth)

    testRuntimeOnly(libs.junit.platformLauncher)
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

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

kotlin {
    compilerOptions {
        jvmToolchain(11)
        jvmTarget.set(JvmTarget.JVM_11)
        allWarningsAsErrors.set(true)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

buildConfig {
    useKotlinOutput { topLevelConstants = true }
    packageName.set("com.parmet.squashlambdas")
    buildConfigField(String::class.java, "GIT_SHA", getGitSha())
}

fun getGitSha(): String =
    providers.exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
    }.standardOutput.asText.get().trim()
