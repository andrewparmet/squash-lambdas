package com.parmet.squashlambdas.di

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
import com.parmet.squashlambdas.aws.DynamoDbMapperProvider
import com.parmet.squashlambdas.aws.ObjectStorage
import com.parmet.squashlambdas.aws.S3ObjectStorage
import com.parmet.squashlambdas.aws.SnsTopicPublisher
import com.parmet.squashlambdas.aws.TopicPublisher
import com.parmet.squashlambdas.cal.CalendarProvider
import com.parmet.squashlambdas.cal.ChangeSummaryResolver
import com.parmet.squashlambdas.cal.ClubLockerChangeSummaryResolver
import com.parmet.squashlambdas.cal.GoogleCalendarProvider
import com.parmet.squashlambdas.clublocker.ClubLockerClient
import com.parmet.squashlambdas.clublocker.TokenManager
import com.parmet.squashlambdas.configureClubLockerResources
import com.parmet.squashlambdas.configureNotifier
import com.parmet.squashlambdas.loadConfiguration
import com.parmet.squashlambdas.notify.Notifier
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.time.measureTime

private val logger = KotlinLogging.logger { }

@BindingContainer
object EmailNotificationModule {
    @Provides
    @SingleIn(AppScope::class)
    fun provideConfig(@Named("configName") configName: String): EmailNotificationConfig =
        withTiming { loadConfiguration(configName) }

    @Provides
    fun provideClubLockerConfig(config: EmailNotificationConfig): ClubLockerConfig =
        config.clubLocker

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
object ChangeSummaryResolverModule {
    @Provides
    fun provideChangeSummaryResolver(resolver: ClubLockerChangeSummaryResolver): ChangeSummaryResolver =
        resolver
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
            AwsClients(
                objectStorage = S3ObjectStorage(),
                topicPublisher = SnsTopicPublisher(),
                dynamoDb = DynamoDbMapperProvider(),
            )
        }
    }

    @Provides
    @SingleIn(AppScope::class)
    fun provideObjectStorage(): ObjectStorage =
        awsClients.objectStorage

    @Provides
    @SingleIn(AppScope::class)
    fun provideDynamoDb(): DynamoDbMapperProvider =
        awsClients.dynamoDb

    @Provides
    @SingleIn(AppScope::class)
    fun provideTopicPublisher(): TopicPublisher =
        awsClients.topicPublisher
}

private data class AwsClients(
    val objectStorage: ObjectStorage,
    val topicPublisher: TopicPublisher,
    val dynamoDb: DynamoDbMapperProvider
)

@BindingContainer
object NotifierModule {
    @Provides
    @SingleIn(AppScope::class)
    @Named("myNotifier")
    fun provideMyNotifier(config: SnsConfig, topicPublisher: TopicPublisher): Notifier =
        withTiming { configureNotifier(config.myTopicArn, topicPublisher) }

    @Provides
    @SingleIn(AppScope::class)
    @Named("publicNotifier")
    fun providePublicNotifier(config: SnsConfig, topicPublisher: TopicPublisher): Notifier =
        withTiming { configureNotifier(config.publicTopicArn!!, topicPublisher) }
}

@BindingContainer
object ClubLockerModule {
    @Provides
    @SingleIn(AppScope::class)
    fun provideTokenManager(config: ClubLockerConfig, objectStorage: ObjectStorage): TokenManager =
        withTiming { TokenManager(config, objectStorage) }

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
    fun provideCalendarProvider(provider: GoogleCalendarProvider): CalendarProvider =
        provider
}

private inline fun <reified T> withTiming(block: () -> T): T =
    withTiming(T::class.simpleName ?: "component", block)

private inline fun <T> withTiming(name: String, block: () -> T): T {
    val result: T
    val time = measureTime { result = block() }
    logger.info { "Finished building $name in $time" }
    return result
}
