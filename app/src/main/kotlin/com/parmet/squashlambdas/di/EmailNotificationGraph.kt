package com.parmet.squashlambdas.di

import com.parmet.squashlambdas.EmailNotificationConfig
import com.parmet.squashlambdas.email.EmailNotificationProcessor
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides

@DependencyGraph(
    scope = AppScope::class,
    bindingContainers = [
        EmailNotificationModule::class,
        ClubLockerModule::class,
        AwsModule::class,
        NotifierModule::class
    ]
)
interface EmailNotificationGraph : EmailNotificationProcessorProvider {
    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides config: EmailNotificationConfig): EmailNotificationGraph
    }
}

interface EmailNotificationProcessorProvider {
    val processor: EmailNotificationProcessor
}
