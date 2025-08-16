package com.parmet.squashlambdas

import com.google.api.services.calendar.Calendar
import com.parmet.squashlambdas.activity.Player
import com.parmet.squashlambdas.clublocker.ClubLockerClient
import com.parmet.squashlambdas.notify.Notifier
import dagger.Module
import dagger.Provides
import io.github.oshai.kotlinlogging.KotlinLogging
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.sns.SnsClient
import javax.inject.Named
import javax.inject.Singleton

private val logger = KotlinLogging.logger { }

@Module
object EmailNotificationModule {
    @Provides
    @Named("configName")
    fun configName(): String =
        "production-email-notification-handler.yml"

    @Provides
    @Singleton
    fun provideConfig(@Named("configName") configName: String): EmailNotificationConfig =
        loadConfiguration<EmailNotificationConfig>(configName)
            .apply { logger.info { "Finished building $this" } }

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
    fun provideCalendar(config: GoogleCalConfig, s3: S3Client): Calendar =
        configureCalendar(config, s3)
            .apply { logger.info { "Finished building $this" } }
}

@Module
object MakeReservationModule {
    @Provides
    @Named("configName")
    fun configName(): String =
        "production-make-reservation-handler.yml"

    @Provides
    @Singleton
    fun provideConfig(@Named("configName") configName: String): MakeReservationConfig =
        loadConfiguration(configName)

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
    @Named("configName")
    fun configName(): String =
        "production-monitor-slots-handler.yml"

    @Provides
    @Singleton
    fun provideConfig(@Named("configName") configName: String): MonitorSlotsConfig =
        loadConfiguration(configName)

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
        S3Client.create()
            .apply { logger.info { "Finished building $this" } }

    @Provides
    @Singleton
    fun provideDynamoDb(): DynamoDbClient =
        DynamoDbClient.create()
            .apply { logger.info { "Finished building $this" } }

    @Provides
    @Singleton
    fun provideSnsClient(): SnsClient =
        SnsClient.create()
            .apply { logger.info { "Finished building $this" } }

    @Provides
    @Singleton
    @Named("myNotifier")
    fun provideMyNotifier(config: SnsConfig, snsClient: SnsClient): Notifier =
        configureNotifier(config.myTopicArn, snsClient)
            .apply { logger.info { "Finished building $this" } }

    @Provides
    @Singleton
    @Named("publicNotifier")
    fun providePublicNotifier(config: SnsConfig, snsClient: SnsClient): Notifier =
        configureNotifier(config.publicTopicArn!!, snsClient)
            .apply { logger.info { "Finished building $this" } }
}

@Module
object ClubLockerModule {
    @Provides
    @Singleton
    fun provideClubLockerClient(config: ClubLockerConfig, s3: S3Client): ClubLockerClient =
        configureClubLockerClient(config, s3).first

    @Provides
    @Singleton
    fun provideHostPlayer(config: ClubLockerConfig, s3: S3Client): Player =
        configureClubLockerClient(config, s3).second
}
