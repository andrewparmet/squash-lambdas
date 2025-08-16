package com.parmet.squashlambdas.integration

import com.google.api.services.calendar.Calendar
import com.parmet.squashlambdas.EmailNotificationHandler
import com.parmet.squashlambdas.SnsConfig
import com.parmet.squashlambdas.configureNotifier
import com.parmet.squashlambdas.dagger.EmailNotificationComponent
import com.parmet.squashlambdas.dagger.EmailNotificationModule
import com.parmet.squashlambdas.notify.Notifier
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.sns.SnsClient
import javax.inject.Named
import javax.inject.Singleton

@Module
class EmailNotificationTestModule(
    private val calendar: Calendar,
    private val s3: S3Client,
    private val sns: SnsClient
) {
    @Provides
    fun calendar(): Calendar =
        calendar

    @Provides
    fun s3Client(): S3Client =
        s3

    @Provides
    fun snsClient(): SnsClient =
        sns

    @Provides
    @Named("myNotifier")
    fun myNotifier(config: SnsConfig, sns: SnsClient): Notifier =
        configureNotifier(config.myTopicArn, sns)
}

@Singleton
@Component(modules = [EmailNotificationTestModule::class, EmailNotificationModule::class])
interface EmailNotificationTestComponent : EmailNotificationComponent {
    override fun inject(target: EmailNotificationHandler)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun configName(@Named("configName") configName: String): Builder

        fun emailNotificationTestModule(module: EmailNotificationTestModule): Builder

        fun build(): EmailNotificationTestComponent
    }
}
