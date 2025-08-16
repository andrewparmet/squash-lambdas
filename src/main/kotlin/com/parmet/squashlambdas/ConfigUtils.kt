package com.parmet.squashlambdas

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import com.google.common.io.Files
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
import java.nio.charset.StandardCharsets.UTF_8
import java.time.LocalTime

fun loadConfiguration(file: String): AppConfig =
    ConfigLoaderBuilder.default()
        .addResourceSource("/com/parmet/squashlambdas/$file")
        .build()
        .loadConfigOrThrow<AppConfig>()

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

fun configureCalendar(config: AppConfig, s3: S3Client) =
    Calendar.Builder(
        GoogleNetHttpTransport.newTrustedTransport(),
        GsonFactory(),
        HttpCredentialsAdapter(
            loadCredentials(config, s3).createScoped(listOf(CalendarScopes.CALENDAR))
        )
    )
        .setApplicationName("PARMET_SQUASH_LAMBDAS")
        .build()

private fun loadCredentials(config: AppConfig, s3: S3Client) =
    GoogleCredentials.fromStream(loadFile(config, "google.cal.creds", s3).byteInputStream(UTF_8))

fun configureClubLockerClient(config: AppConfig, s3: S3Client): Pair<ClubLockerClient, Player> {
    val creds: Map<String, String> = Gson().fromJson(loadFile(config, "clubLocker.creds", s3))

    val hostPlayer =
        Player(
            email = creds.getValue("username"),
            name = config.clubLocker.name
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

fun getSchedule(config: AppConfig, s3: S3Client) =
    Schedule.fromString(loadFile(config, "schedule", s3))

fun getPreferredCourts(config: AppConfig, s3: S3Client) =
    getPreferredCourts(loadFile(config, "courts", s3))

fun getPreferredCourts(s: String) =
    s.mapNonEmptyLines { Court.valueOf(it) }

fun getPreferredTimes(config: AppConfig, s3: S3Client) =
    getPreferredTimes(loadFile(config, "times", s3))

fun getPreferredTimes(s: String) =
    s.mapNonEmptyLines { LocalTime.parse(it) }

private fun loadFile(config: AppConfig, configKey: String, s3: S3Client): String {
    val fileConfig = getFileConfig(config, configKey)
    return if (fileConfig.location == "local") {
        Files.asCharSource(File(fileConfig.fileName), UTF_8).read()
    } else {
        check("s3" == fileConfig.location)
        val awsFileConfig = getAwsFileConfig(config, configKey)
        s3.getObjectAsBytes(
            GetObjectRequest.builder()
                .bucket(awsFileConfig.bucket)
                .key(awsFileConfig.key)
                .build()
        ).asUtf8String()
    }
}

private fun getFileConfig(config: AppConfig, configKey: String): FileConfig =
    when (configKey) {
        "google.cal.creds" -> config.google.cal.creds
        "clubLocker.creds" -> config.clubLocker.creds
        "schedule" -> config.schedule
        "courts" -> config.courts
        "times" -> config.times
        else -> error("Unknown config key: $configKey")
    }

private fun getAwsFileConfig(config: AppConfig, configKey: String): AwsFileConfig =
    when (configKey) {
        "google.cal.creds" -> config.aws.google.cal.creds
        "clubLocker.creds" -> config.aws.clubLocker.creds
        "schedule" -> config.aws.schedule
        "courts" -> config.aws.courts
        "times" -> config.aws.times
        else -> error("Unknown AWS config key: $configKey")
    }
