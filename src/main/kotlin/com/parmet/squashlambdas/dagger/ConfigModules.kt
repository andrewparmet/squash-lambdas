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
import dagger.Module
import dagger.Provides
import io.github.oshai.kotlinlogging.KotlinLogging
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
}

@Module
object AwsModule {
    @Provides
    @Singleton
    fun provideS3(): S3Client =
        withTiming { S3Client.create() }

    @Provides
    @Singleton
    fun provideDynamoDb(): DynamoDbClient =
        withTiming { DynamoDbClient.create() }

    @Provides
    @Singleton
    fun provideSnsClient(): SnsClient =
        withTiming { SnsClient.create() }

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
    fun provideClubLockerResources(config: ClubLockerConfig, s3: S3Client) =
        withTiming { configureClubLockerResources(config, s3) }

    @Provides
    @Singleton
    fun provideClubLockerClient(resources: ClubLockerResources) =
        resources.client

    @Provides
    @Singleton
    fun provideHostPlayer(resources: ClubLockerResources) =
        resources.player
}

@Module
object CalendarModule {
    @Provides
    @Singleton
    fun provideCalendar(config: GoogleCalConfig, s3: S3Client): Calendar =
        configureCalendar(config, s3)
            .apply { logger.info { "Finished building $this" } }
}

private inline fun <reified T> withTiming(block: () -> T): T {
    val result: T
    val time = measureTime { result = block() }
    logger.info { "Finished building $result in $time" }
    return result
}
