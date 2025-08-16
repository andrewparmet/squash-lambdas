package com.parmet.squashlambdas

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import com.google.gson.Gson
import com.parmet.squashlambdas.Context.context
import com.parmet.squashlambdas.activity.Court
import com.parmet.squashlambdas.activity.Player
import com.parmet.squashlambdas.activity.valueOf
import com.parmet.squashlambdas.clublocker.ClubLockerClient
import com.parmet.squashlambdas.clublocker.ClubLockerClientImpl
import com.parmet.squashlambdas.clublocker.ClubLockerUser
import com.parmet.squashlambdas.notify.Notifier
import com.parmet.squashlambdas.reserve.Schedule
import com.parmet.squashlambdas.reserve.mapNonEmptyLines
import com.parmet.squashlambdas.util.fromJson
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.sns.SnsClient
import java.io.File
import java.time.LocalTime

inline fun <reified T : Any> loadConfiguration(file: String): T =
    ConfigLoaderBuilder.default()
        .addResourceSource("/com/parmet/squashlambdas/$file")
        .build()
        .loadConfigOrThrow<T>()

fun configureS3(): S3Client =
    S3Client.create()

fun configureNotifier(topicArn: String) =
    Notifier(
        SnsClient.create(),
        topicArn,
        context
    )

fun configureDynamoDb(): DynamoDbClient =
    DynamoDbClient.create()

fun configureCalendar(config: GoogleCalConfig, s3: S3Client) =
    Calendar.Builder(
        GoogleNetHttpTransport.newTrustedTransport(),
        GsonFactory(),
        HttpCredentialsAdapter(
            loadCredentials(config.fileConfig, s3).createScoped(listOf(CalendarScopes.CALENDAR))
        )
    )
        .setApplicationName("PARMET_SQUASH_LAMBDAS")
        .build()

private fun loadCredentials(config: FileConfig, s3: S3Client) =
    GoogleCredentials.fromStream(loadFile(config, s3).byteInputStream())

fun configureClubLockerClient(config: ClubLockerConfig, s3: S3Client): Pair<ClubLockerClient, Player> {
    val creds: Map<String, String> = Gson().fromJson(loadFile(config.fileConfig, s3))

    val hostPlayer =
        Player(
            email = creds.getValue("username"),
            name = config.name
        )

    return Pair(
        ClubLockerClientImpl(
            ClubLockerUser(
                hostPlayer.email!!,
                creds.getValue("password"),
            )
        ),
        hostPlayer
    )
}

fun getSchedule(config: FileConfig, s3: S3Client) =
    Schedule.fromString(loadFile(config, s3))

fun getPreferredCourts(config: FileConfig, s3: S3Client) =
    getPreferredCourts(loadFile(config, s3))

fun getPreferredCourts(s: String) =
    s.mapNonEmptyLines { Court.valueOf(it) }

fun getPreferredTimes(config: FileConfig, s3: S3Client) =
    getPreferredTimes(loadFile(config, s3))

fun getPreferredTimes(s: String) =
    s.mapNonEmptyLines { LocalTime.parse(it) }

private fun loadFile(config: FileConfig, s3Client: S3Client): String =
    when (config.location) {
        "s3" ->
            s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                    .bucket(config.bucket)
                    .key(config.key)
                    .build()
            ).asUtf8String()

        "local" ->
            File(config.fileName!!).readText()

        else -> error("unsupported location")
    }
