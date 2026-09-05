plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.shadow)
    alias(libs.plugins.spotless)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.aws.kotlin.lambda)
    implementation(libs.aws.kotlin.resource.groups.tagging.api)
    implementation(libs.aws.kotlin.s3)
    implementation(libs.aws.kotlin.ssm)
    implementation(libs.clikt)
    implementation(libs.google.calendar)
    implementation(libs.google.oauth2.http)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testRuntimeOnly(libs.junit.platformLauncher)
    runtimeOnly(libs.slf4j.simple)
}

spotless {
    kotlin {
        ktlint()
            .editorConfigOverride(
                mapOf(
                    "max_line_length" to 120,
                    "ktlint_function_signature_body_expression_wrapping" to "always",
                    "ktlint_standard_trailing-comma-on-call-site" to "disabled",
                    "ktlint_standard_trailing-comma-on-declaration-site" to "disabled",
                    "ij_kotlin_packages_to_use_import_on_demand" to null,
                )
            )
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jvmTarget.get()))
    }
}

kotlin {
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveBaseName.set("squash-infra")
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
    exclude("META-INF/*.kotlin_module")
    exclude("META-INF/maven/**")
}

tasks.register<JavaExec>("deploy") {
    dependsOn(":app:spotlessCheck", ":app:test", ":app:shadowJar", "shadowJar")
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.parmet.squashlambdas.infra.DeployKt")
    args(rootProject.projectDir.absolutePath)
    standardInput = System.`in`
}

tasks.register<JavaExec>("provisionUser") {
    dependsOn(":app:spotlessCheck", ":app:test", ":app:shadowJar", "shadowJar")
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.parmet.squashlambdas.infra.ProvisionUserKt")
    args(rootProject.projectDir.absolutePath)
    providers.gradleProperty("user").orNull?.let { args("--user", it) }
    providers.gradleProperty("shareWith").orNull?.split(",")?.forEach { args("--share-with", it) }
}
