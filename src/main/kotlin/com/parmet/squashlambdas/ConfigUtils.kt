package com.parmet.squashlambdas

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.common.base.Preconditions.checkState
import org.apache.commons.configuration2.Configuration
import org.apache.commons.configuration2.XMLConfiguration
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder
import org.apache.commons.configuration2.builder.fluent.Parameters
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.Paths

internal fun loadConfiguration(file: String) =
    FileBasedConfigurationBuilder(XMLConfiguration::class.java)
        .configure(
            Parameters()
                .xml()
                .setURL(object : Any() {}.javaClass.getResource(file))
                .setValidating(false)
                .setThrowExceptionOnMissing(true))
        .configuration

internal fun configureS3() = AmazonS3ClientBuilder.defaultClient()

internal fun configureSns() = AmazonSNSClientBuilder.defaultClient()

internal fun configureCalendar(config: Configuration, s3: AmazonS3) =
    Calendar.Builder(
        GoogleNetHttpTransport.newTrustedTransport(),
        JacksonFactory.getDefaultInstance(),
        loadCredential(config, s3).createScoped(listOf(CalendarScopes.CALENDAR))
    )
        .setApplicationName("PARMET_SQUASH_LAMBDAS")
        .build()

private fun loadCredential(config: Configuration, s3: AmazonS3): GoogleCredential {
    val credsLocation = config.getString("google.credsLocation")
    if ("local" == credsLocation) {
        return GoogleCredential.fromStream(
            Files.newInputStream(Paths.get(config.getString("google.credsFileName"))))
    }
    checkState("s3" == credsLocation)
    return GoogleCredential.fromStream(
        s3.getObjectAsString(
            config.getString("aws.googleCalCreds.bucket"),
            config.getString("aws.googleCalCreds.key"))
            .byteInputStream(UTF_8))
}
