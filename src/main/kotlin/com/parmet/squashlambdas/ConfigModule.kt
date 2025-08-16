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

class ConfigModule : AbstractModule() {
    @Provides
    @Singleton
    fun provideAppConfig(): AppConfig =
        loadConfiguration(System.getenv("CONFIG_NAME") + ".yml")

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
    fun provideCalendar(config: AppConfig, s3: S3Client): Calendar =
        configureCalendar(config, s3)

    @Provides
    @Singleton
    fun provideClubLockerClient(config: AppConfig, s3: S3Client): ClubLockerClient =
        configureClubLockerClient(config, s3).first

    @Provides
    @Singleton
    fun provideHostPlayer(config: AppConfig, s3: S3Client): Player =
        configureClubLockerClient(config, s3).second

    @Provides
    @Singleton
    @Named("myNotifier")
    fun provideMyNotifier(config: AppConfig): Notifier =
        configureNotifier(config.aws.sns.myTopicArn)

    @Provides
    @Singleton
    @Named("publicNotifier")
    fun providePublicNotifier(config: AppConfig): Notifier =
        configureNotifier(config.aws.sns.publicTopicArn)
}
