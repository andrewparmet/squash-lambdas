package com.parmet.squashlambdas.dagger

import com.google.api.services.calendar.Calendar
import com.parmet.squashlambdas.ClubLockerConfig
import com.parmet.squashlambdas.ClubLockerResources
import com.parmet.squashlambdas.EmailNotificationConfig
import com.parmet.squashlambdas.GoogleCalConfig
import com.parmet.squashlambdas.MakeReservationConfig
import com.parmet.squashlambdas.MonitorSlotsConfig
import com.parmet.squashlambdas.SnsConfig
import com.parmet.squashlambdas.configureCalendar
import com.parmet.squashlambdas.configureClubLockerResources
import com.parmet.squashlambdas.configureNotifier
import com.parmet.squashlambdas.loadConfiguration
import com.parmet.squashlambdas.notify.Notifier
import com.parmet.squashlambdas.util.FileLoader
import dagger.Module
import dagger.Provides
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.sns.SnsClient
import javax.inject.Named
import javax.inject.Singleton
import kotlin.time.measureTime

private val logger = KotlinLogging.logger { }

@Module
object EmailNotificationModule {
    @Provides
    @Singleton
    fun provideConfig(@Named("configName") configName: String): EmailNotificationConfig =
        withTiming { loadConfiguration(configName) }

    @Provides
    @Singleton
    fun provideGoogleCalConfig(config: EmailNotificationConfig) =
        config.googleCal

    @Provides
    @Singleton
    fun provideNotifierConfig(config: EmailNotificationConfig) =
        config.sns

    @Provides
    @Singleton
    fun provideTokenUpdateConfig(config: EmailNotificationConfig) =
        config.tokenUpdate
}

@Module
object MakeReservationModule {
    @Provides
    @Singleton
    fun provideConfig(@Named("configName") configName: String): MakeReservationConfig =
        withTiming { loadConfiguration(configName) }

    @Provides
    @Singleton
    fun provideClubLockerConfig(config: MakeReservationConfig) =
        config.clubLocker

    @Provides
    @Singleton
    fun provideNotifierConfig(config: MakeReservationConfig) =
        config.sns
}

@Module
object MonitorSlotsModule {
    @Provides
    @Singleton
    fun provideConfig(@Named("configName") configName: String): MonitorSlotsConfig =
        withTiming { loadConfiguration(configName) }

    @Provides
    @Singleton
    fun provideClubLockerConfig(config: MonitorSlotsConfig) =
        config.clubLocker

    @Provides
    @Singleton
    fun provideNotifierConfig(config: MonitorSlotsConfig) =
        config.sns

    @Provides
    @Singleton
    fun provideDynamoDbConfig(config: MonitorSlotsConfig) =
        config.dynamoDb
}

@Module
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
    @Singleton
    fun provideS3(): S3Client =
        awsClients.s3

    @Provides
    @Singleton
    fun provideDynamoDb(): DynamoDbClient =
        awsClients.dynamoDb

    @Provides
    @Singleton
    fun provideSnsClient(): SnsClient =
        awsClients.sns
}

private data class AwsClients(
    val s3: S3Client,
    val sns: SnsClient,
    val dynamoDb: DynamoDbClient
)

@Module
object NotifierModule {
    @Provides
    @Singleton
    @Named("myNotifier")
    fun provideMyNotifier(config: SnsConfig, snsClient: SnsClient): Notifier =
        withTiming { configureNotifier(config.myTopicArn, snsClient) }

    @Provides
    @Singleton
    @Named("publicNotifier")
    fun providePublicNotifier(config: SnsConfig, snsClient: SnsClient): Notifier =
        withTiming { configureNotifier(config.publicTopicArn!!, snsClient) }
}

@Module
object ClubLockerModule {
    @Provides
    @Singleton
    fun provideClubLockerResources(config: ClubLockerConfig, fileLoader: FileLoader) =
        withTiming { configureClubLockerResources(config, fileLoader) }

    @Provides
    @Singleton
    fun provideClubLockerClient(resources: ClubLockerResources) =
        withTiming { resources.client.apply { init() } }

    @Provides
    @Singleton
    fun provideHostPlayer(resources: ClubLockerResources) =
        resources.player
}

@Module
object CalendarModule {
    @Provides
    @Singleton
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
