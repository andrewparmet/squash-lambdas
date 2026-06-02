package com.parmet.squashlambdas.integration

import com.google.api.services.calendar.Calendar
import com.parmet.squashlambdas.di.EmailNotificationInjector
import com.parmet.squashlambdas.di.EmailNotificationModule
import com.parmet.squashlambdas.di.NotifierModule
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.sns.SnsClient

@DependencyGraph(
    scope = AppScope::class,
    bindingContainers = [
        EmailNotificationModule::class,
        NotifierModule::class
    ]
)
interface EmailNotificationTestGraph : EmailNotificationInjector {
    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides @Named("configName") configName: String,
            @Provides @SingleIn(AppScope::class) calendar: Calendar,
            @Provides @SingleIn(AppScope::class) s3Client: S3Client,
            @Provides @SingleIn(AppScope::class) snsClient: SnsClient
        ): EmailNotificationTestGraph
    }
}
