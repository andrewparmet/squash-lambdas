package com.parmet.squashlambdas

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
import com.parmet.squashlambdas.clublocker.TokenManager
import com.parmet.squashlambdas.notify.Notifier
import com.parmet.squashlambdas.util.FileLoader
import com.typesafe.config.ConfigFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.serializer
import software.amazon.awssdk.services.sns.SnsClient

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T : Any> loadConfiguration(file: String): T {
    val config =
        ConfigFactory.parseResources("com/parmet/squashlambdas/$file")
            .resolveWith(ConfigFactory.systemEnvironment())
    return Hocon.decodeFromConfig(serializer(), config)
}

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

fun configureClubLockerResources(config: ClubLockerConfig, tokenManager: TokenManager): ClubLockerResources {
    val hostPlayer =
        Player(
            email = config.email,
            name = config.name
        )

    return ClubLockerResources(
        ClubLockerClientImpl(tokenManager),
        hostPlayer
    )
}
