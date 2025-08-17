package com.parmet.squashlambdas

import com.fasterxml.jackson.module.kotlin.readValue
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import com.parmet.squashlambdas.Context.context
import com.parmet.squashlambdas.activity.Player
import com.parmet.squashlambdas.clublocker.ClubLockerClient
import com.parmet.squashlambdas.clublocker.ClubLockerClientImpl
import com.parmet.squashlambdas.clublocker.ClubLockerUser
import com.parmet.squashlambdas.json.Json
import com.parmet.squashlambdas.notify.Notifier
import com.parmet.squashlambdas.util.FileLoader
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource
import software.amazon.awssdk.services.sns.SnsClient

inline fun <reified T : Any> loadConfiguration(file: String): T =
    ConfigLoaderBuilder.default()
        .addResourceSource("/com/parmet/squashlambdas/$file")
        .build()
        .loadConfigOrThrow<T>()

fun configureNotifier(topicArn: String, snsClient: SnsClient) =
    Notifier(
        snsClient,
        topicArn,
        context
    )

fun configureCalendar(config: GoogleCalConfig, fileLoader: FileLoader) =
    Calendar.Builder(
        GoogleNetHttpTransport.newTrustedTransport(),
        GsonFactory(),
        HttpCredentialsAdapter(
            GoogleCredentials.fromStream(
                fileLoader.streamFile(config.creds)
            ).createScoped(listOf(CalendarScopes.CALENDAR))
        )
    )
        .setApplicationName("PARMET_SQUASH_LAMBDAS")
        .build()

data class ClubLockerResources(
    val client: ClubLockerClient,
    val player: Player
)

fun configureClubLockerResources(config: ClubLockerConfig, fileLoader: FileLoader): ClubLockerResources {
    val creds: Map<String, String> = fileLoader.streamFile(config.creds).use { Json.mapper.readValue(it) }

    val hostPlayer =
        Player(
            email = creds.getValue("username"),
            name = config.name
        )

    return ClubLockerResources(
        ClubLockerClientImpl(
            ClubLockerUser(
                hostPlayer.email!!,
                creds.getValue("password"),
            )
        ),
        hostPlayer
    )
}
