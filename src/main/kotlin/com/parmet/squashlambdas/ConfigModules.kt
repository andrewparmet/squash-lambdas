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
import software.amazon.awssdk.services.sns.SnsClient

class EmailNotificationModule : AbstractModule() {
    @Provides
    @Named("configName")
    fun provideConfigName() =
        "production-email-notification-handler.yml"

    @Provides
    @Singleton
    fun provideConfig(@Named("configName") configName: String): EmailNotificationConfig =
        loadConfiguration(configName)

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
}

class MakeReservationModule : AbstractModule() {
    @Provides
    @Named("configName")
    fun provideConfigName() =
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

class MonitorSlotsModule : AbstractModule() {
    @Provides
    @Named("configName")
    fun provideConfigName() =
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

class AwsModule : AbstractModule() {
    @Provides
    @Singleton
    fun provideS3(): S3Client =
        S3Client.create()

    @Provides
    @Singleton
    fun provideDynamoDb(): DynamoDbClient =
        DynamoDbClient.create()

    @Provides
    @Singleton
    fun provideSnsClient() =
        SnsClient.create()

    @Provides
    @Singleton
    @Named("myNotifier")
    fun provideMyNotifier(config: SnsConfig, snsClient: SnsClient): Notifier =
        configureNotifier(config.myTopicArn, snsClient)

    @Provides
    @Singleton
    @Named("publicNotifier")
    fun providePublicNotifier(config: SnsConfig, snsClient: SnsClient): Notifier =
        configureNotifier(config.publicTopicArn, snsClient)
}

class ClubLockerModule : AbstractModule() {
    @Provides
    @Singleton
    fun provideClubLockerClient(config: ClubLockerConfig, s3: S3Client): ClubLockerClient =
        configureClubLockerClient(config, s3).first

    @Provides
    @Singleton
    fun provideHostPlayer(config: ClubLockerConfig, s3: S3Client): Player =
        configureClubLockerClient(config, s3).second
}
