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
import com.google.common.io.Files
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.parmet.squashlambdas.activity.Player
import com.parmet.squashlambdas.reserve.ClubLockerClient
import com.parmet.squashlambdas.reserve.ClubLockerClientImpl
import com.parmet.squashlambdas.reserve.Schedule
import org.apache.commons.configuration2.Configuration
import org.apache.commons.configuration2.XMLConfiguration
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder
import org.apache.commons.configuration2.builder.fluent.Parameters
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8

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
            Files.asByteSource(File(config.getString("google.credsFileName"))).openStream())
    }
    checkState("s3" == credsLocation)
    return GoogleCredential.fromStream(
        s3.getObjectAsString(
            config.getString("aws.googleCalCreds.bucket"),
            config.getString("aws.googleCalCreds.key")
        ).byteInputStream(UTF_8))
}

internal fun configureClubLockerClient(config: Configuration, s3: AmazonS3): Pair<ClubLockerClient, Player> {
    val credsLocation = config.getString("clubLocker.credsLocation")

    val creds: Map<String, String> =
        Gson().fromJson(
            if ("local" == credsLocation) {
                Files.asCharSource(File(config.getString("clubLocker.credsFileName")), UTF_8).read()
            } else {
                checkState("s3" == credsLocation)

                s3.getObjectAsString(
                    config.getString("aws.clubLockerCreds.bucket"),
                    config.getString("aws.clubLockerCreds.key"))
        }, object : TypeToken<Map<String, String>>() {}.type)

    val hostPlayer = Player.withEmail(creds.getValue("username"))

    return Pair(
        ClubLockerClientImpl(
            hostPlayer.email!!,
            creds.getValue("password")
        ),
        hostPlayer)
}

internal fun getSchedule(config: Configuration, s3: AmazonS3): Schedule {
    val scheduleLocation = config.getString("schedule.location")

    return if ("local" == scheduleLocation) {
        Schedule.fromString(
            Files.asCharSource(File(config.getString("schedule.fileName")), UTF_8).read()
        )
    } else {
        checkState("s3" == scheduleLocation)

        Schedule.fromS3(
            s3,
            config.getString("aws.reservationSchedule.bucket"),
            config.getString("aws.reservationSchedule.key"))
    }
}
