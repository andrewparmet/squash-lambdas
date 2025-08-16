package com.parmet.squashlambdas

import com.google.api.services.calendar.Calendar
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import com.google.inject.name.Named
import com.parmet.squashlambdas.activity.Player
import com.parmet.squashlambdas.clublocker.ClubLockerClient
import com.parmet.squashlambdas.notify.Notifier
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.s3.S3Client

class EmailNotificationModule : AbstractModule() {
    @Provides
    @Singleton
    fun provideEmailNotificationConfig(): EmailNotificationConfig =
        loadConfiguration(System.getenv("CONFIG_NAME") + ".yml")

    @Provides
    @Singleton
    fun provideGoogleCalConfig(config: EmailNotificationConfig) =
        config.googleCal

    @Provides
    @Singleton
    fun provideNotifierConfig(config: EmailNotificationConfig) =
        config.sns
}

class MakeReservationModule : AbstractModule() {
    @Provides
    @Singleton
    fun provideMakeReservationConfig(): MakeReservationConfig =
        loadConfiguration(System.getenv("CONFIG_NAME") + ".yml")

    @Provides
    @Singleton
    fun provideClubLockerConfig(config: MakeReservationConfig) =
        config.clubLocker

    @Provides
    @Singleton
    fun provideNotifierConfig(config: MakeReservationConfig) =
        config.sns
}

class MonitorSlotsModule : AbstractModule() {
    @Provides
    @Singleton
    fun provideMonitorSlotsConfig(): MonitorSlotsConfig =
        loadConfiguration(System.getenv("CONFIG_NAME") + ".yml")

    @Provides
    @Singleton
    fun provideClubLockerConfig(config: MonitorSlotsConfig) =
        config.clubLocker

    @Provides
    @Singleton
    fun provideNotifierConfig(config: MonitorSlotsConfig) =
        config.sns
}

class ConfigModule : AbstractModule() {
    @Provides
    @Singleton
    fun provideS3(): S3Client =
        configureS3()

    @Provides
    @Singleton
    fun provideDynamoDb(): DynamoDbClient =
        configureDynamoDb()

    @Provides
    @Singleton
    fun provideCalendar(config: GoogleCalConfig, s3: S3Client): Calendar =
        configureCalendar(config, s3)

    @Provides
    @Singleton
    fun provideClubLockerClient(config: ClubLockerConfig, s3: S3Client): ClubLockerClient =
        configureClubLockerClient(config, s3).first

    @Provides
    @Singleton
    fun provideHostPlayer(config: ClubLockerConfig, s3: S3Client): Player =
        configureClubLockerClient(config, s3).second

    @Provides
    @Singleton
    @Named("myNotifier")
    fun provideMyNotifier(config: SnsConfig): Notifier =
        configureNotifier(config.myTopicArn)

    @Provides
    @Singleton
    @Named("publicNotifier")
    fun providePublicNotifier(config: SnsConfig): Notifier =
        configureNotifier(config.publicTopicArn)
}
