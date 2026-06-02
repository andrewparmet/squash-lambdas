package com.parmet.squashlambdas.di

import com.google.api.services.calendar.Calendar
import com.parmet.squashlambdas.ClubLockerConfig
import com.parmet.squashlambdas.ClubLockerResources
import com.parmet.squashlambdas.DynamoDbConfig
import com.parmet.squashlambdas.EmailNotificationConfig
import com.parmet.squashlambdas.GoogleCalConfig
import com.parmet.squashlambdas.MakeReservationConfig
import com.parmet.squashlambdas.MonitorSlotsConfig
import com.parmet.squashlambdas.SnsConfig
import com.parmet.squashlambdas.TokenUpdateConfig
import com.parmet.squashlambdas.activity.Player
import com.parmet.squashlambdas.clublocker.ClubLockerClient
import com.parmet.squashlambdas.clublocker.TokenManager
import com.parmet.squashlambdas.configureCalendar
import com.parmet.squashlambdas.configureClubLockerResources
import com.parmet.squashlambdas.configureNotifier
import com.parmet.squashlambdas.loadConfiguration
import com.parmet.squashlambdas.notify.Notifier
import com.parmet.squashlambdas.util.FileLoader
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.sns.SnsClient
import kotlin.time.measureTime

private val logger = KotlinLogging.logger { }

@BindingContainer
object EmailNotificationModule {
    @Provides
    @SingleIn(AppScope::class)
    fun provideConfig(@Named("configName") configName: String): EmailNotificationConfig =
        withTiming { loadConfiguration(configName) }

    @Provides
    fun provideGoogleCalConfig(config: EmailNotificationConfig): GoogleCalConfig =
        config.googleCal

    @Provides
    fun provideNotifierConfig(config: EmailNotificationConfig): SnsConfig =
        config.sns

    @Provides
    fun provideTokenUpdateConfig(config: EmailNotificationConfig): TokenUpdateConfig =
        config.tokenUpdate
}

@BindingContainer
object MakeReservationModule {
    @Provides
    @SingleIn(AppScope::class)
    fun provideConfig(@Named("configName") configName: String): MakeReservationConfig =
        withTiming { loadConfiguration(configName) }

    @Provides
    fun provideClubLockerConfig(config: MakeReservationConfig): ClubLockerConfig =
        config.clubLocker

    @Provides
    fun provideNotifierConfig(config: MakeReservationConfig): SnsConfig =
        config.sns
}

@BindingContainer
object MonitorSlotsModule {
    @Provides
    @SingleIn(AppScope::class)
    fun provideConfig(@Named("configName") configName: String): MonitorSlotsConfig =
        withTiming { loadConfiguration(configName) }

    @Provides
    fun provideClubLockerConfig(config: MonitorSlotsConfig): ClubLockerConfig =
        config.clubLocker

    @Provides
    fun provideNotifierConfig(config: MonitorSlotsConfig): SnsConfig =
        config.sns

    @Provides
    fun provideDynamoDbConfig(config: MonitorSlotsConfig): DynamoDbConfig =
        config.dynamoDb
}

@BindingContainer
object AwsModule {
    private val awsClients: AwsClients by lazy {
        withTiming("AwsClients") {
            runBlocking {
                coroutineScope {
                    val s3 = async { S3Client.create() }
                    val sns = async { SnsClient.create() }
                    val dynamo = async { DynamoDbClient.create() }
                    AwsClients(s3.await(), sns.await(), dynamo.await())
                }
            }
        }
    }

    @Provides
    @SingleIn(AppScope::class)
    fun provideS3(): S3Client =
        awsClients.s3

    @Provides
    @SingleIn(AppScope::class)
    fun provideDynamoDb(): DynamoDbClient =
        awsClients.dynamoDb

    @Provides
    @SingleIn(AppScope::class)
    fun provideSnsClient(): SnsClient =
        awsClients.sns
}

private data class AwsClients(
    val s3: S3Client,
    val sns: SnsClient,
    val dynamoDb: DynamoDbClient
)

@BindingContainer
object NotifierModule {
    @Provides
    @SingleIn(AppScope::class)
    @Named("myNotifier")
    fun provideMyNotifier(config: SnsConfig, snsClient: SnsClient): Notifier =
        withTiming { configureNotifier(config.myTopicArn, snsClient) }

    @Provides
    @SingleIn(AppScope::class)
    @Named("publicNotifier")
    fun providePublicNotifier(config: SnsConfig, snsClient: SnsClient): Notifier =
        withTiming { configureNotifier(config.publicTopicArn!!, snsClient) }
}

@BindingContainer
object ClubLockerModule {
    @Provides
    @SingleIn(AppScope::class)
    fun provideTokenManager(config: ClubLockerConfig, s3Client: S3Client): TokenManager =
        withTiming { TokenManager(config, s3Client) }

    @Provides
    @SingleIn(AppScope::class)
    fun provideClubLockerResources(config: ClubLockerConfig, tokenManager: TokenManager): ClubLockerResources =
        withTiming { configureClubLockerResources(config, tokenManager) }

    @Provides
    @SingleIn(AppScope::class)
    fun provideClubLockerClient(resources: ClubLockerResources): ClubLockerClient =
        withTiming { resources.client.apply { init() } }

    @Provides
    fun provideHostPlayer(resources: ClubLockerResources): Player =
        resources.player
}

@BindingContainer
object CalendarModule {
    @Provides
    @SingleIn(AppScope::class)
    fun provideCalendar(config: GoogleCalConfig, fileLoader: FileLoader): Calendar =
        withTiming { configureCalendar(config, fileLoader) }
}

private inline fun <reified T> withTiming(block: () -> T): T {
    val result: T
    val time = measureTime { result = block() }
    logger.info { "Finished building $result in $time" }
    return result
}

private inline fun <T> withTiming(name: String, block: () -> T): T {
    val result: T
    val time = measureTime { result = block() }
    logger.info { "Finished building $name in $time" }
    return result
}
