package com.parmet.squashlambdas

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import com.google.common.base.Preconditions.checkState
import com.google.common.io.Files
import com.google.gson.Gson
import com.parmet.squashlambdas.activity.Court
import com.parmet.squashlambdas.activity.Player
import com.parmet.squashlambdas.activity.valueOf
import com.parmet.squashlambdas.clublocker.ClubLockerClient
import com.parmet.squashlambdas.clublocker.ClubLockerClientImpl
import com.parmet.squashlambdas.clublocker.ClubLockerUser
import com.parmet.squashlambdas.notify.Notifier
import com.parmet.squashlambdas.reserve.Schedule
import com.parmet.squashlambdas.reserve.mapNonEmptyLines
import org.apache.commons.configuration2.Configuration
import org.apache.commons.configuration2.XMLConfiguration
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder
import org.apache.commons.configuration2.builder.fluent.Parameters
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8
import java.time.LocalTime

fun loadConfiguration(file: String) =
    FileBasedConfigurationBuilder(XMLConfiguration::class.java)
        .configure(
            Parameters()
                .xml()
                .setURL(object : Any() {}.javaClass.getResource(file))
                .setValidating(false)
                .setThrowExceptionOnMissing(true))
        .configuration

fun configureS3() = AmazonS3ClientBuilder.defaultClient()

fun configureNotifier(config: Configuration) =
    Notifier(
        AmazonSNSClientBuilder.defaultClient(),
        config.getString("aws.sns.myTopicArn"),
        config.getString("aws.sns.publicTopicArn")
    )

fun configureDynamoDb() = AmazonDynamoDBClientBuilder.defaultClient()

fun configureCalendar(config: Configuration, s3: AmazonS3) =
    Calendar.Builder(
        GoogleNetHttpTransport.newTrustedTransport(),
        JacksonFactory.getDefaultInstance(),
        HttpCredentialsAdapter(
            loadCredentials(config, s3).createScoped(listOf(CalendarScopes.CALENDAR))
        )
    )
        .setApplicationName("PARMET_SQUASH_LAMBDAS")
        .build()

private fun loadCredentials(config: Configuration, s3: AmazonS3) =
    GoogleCredentials.fromStream(loadFile(config, "google.cal.creds", s3).byteInputStream(UTF_8))

fun configureClubLockerClient(config: Configuration, s3: AmazonS3): Pair<ClubLockerClient, Player> {
    val creds: Map<String, String> = Gson().fromJson(loadFile(config, "clubLocker.creds", s3))

    val hostPlayer =
        Player(
            email = creds.getValue("username"),
            memberId = config.getInt("clubLocker.memberId")
        )

    return Pair(
        ClubLockerClientImpl(
            ClubLockerUser(
                hostPlayer.email!!,
                creds.getValue("password"),
                hostPlayer.memberId!!
            )
        ),
        hostPlayer
    )
}

fun getSchedule(config: Configuration, s3: AmazonS3) =
    Schedule.fromString(loadFile(config, "schedule", s3))

fun getPreferredCourts(config: Configuration, s3: AmazonS3) =
    getPreferredCourts(loadFile(config, "courts", s3))

fun getPreferredCourts(s: String) =
    s.mapNonEmptyLines { Court.valueOf(it) }

fun getPreferredTimes(config: Configuration, s3: AmazonS3) =
    getPreferredTimes(loadFile(config, "times", s3))

fun getPreferredTimes(s: String) =
    s.mapNonEmptyLines { LocalTime.parse(it) }

private fun loadFile(
    config: Configuration,
    configKey: String,
    s3: AmazonS3
): String =
    config.getString("$configKey.location").let {
        if (it == "local") {
            Files.asCharSource(File(config.getString("$configKey.fileName")), UTF_8).read()
        } else {
            checkState("s3" == it)
            s3.getObjectAsString(
                config.getString("aws.$configKey.bucket"),
                config.getString("aws.$configKey.key"))
        }
    }
