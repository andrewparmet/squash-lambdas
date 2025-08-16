package com.parmet.squashlambdas

import com.google.api.services.calendar.Calendar
import com.google.inject.Provides
import com.google.inject.Singleton
import com.google.inject.name.Named
import com.google.inject.name.Names.named
import com.parmet.squashlambdas.activity.Player
import com.parmet.squashlambdas.clublocker.ClubLockerClient
import com.parmet.squashlambdas.notify.Notifier
import dev.misfitlabs.kotlinguice4.KotlinModule
import io.github.oshai.kotlinlogging.KotlinLogging
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.sns.SnsClient

private val logger = KotlinLogging.logger { }

class EmailNotificationModule : KotlinModule() {
    override fun configure() {
        bind<String>().annotatedWith(named("configName"))
            .toInstance("production-email-notification-handler.yml")
    }

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

class MakeReservationModule : KotlinModule() {
    override fun configure() {
        bind<String>().annotatedWith(named("configName"))
            .toInstance("production-make-reservation-handler.yml")
    }

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

class MonitorSlotsModule : KotlinModule() {
    override fun configure() {
        bind<String>().annotatedWith(named("configName"))
            .toInstance("production-monitor-slots-handler.yml")
    }

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

class AwsModule : KotlinModule() {
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
    fun provideSnsClient() =
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

class ClubLockerModule : KotlinModule() {
    @Provides
    @Singleton
    fun provideClubLockerClient(config: ClubLockerConfig, s3: S3Client): ClubLockerClient =
        configureClubLockerClient(config, s3).first

    @Provides
    @Singleton
    fun provideHostPlayer(config: ClubLockerConfig, s3: S3Client): Player =
        configureClubLockerClient(config, s3).second
}
