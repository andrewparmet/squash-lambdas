package com.parmet.squashlambdas.di

import com.parmet.squashlambdas.EmailNotificationHandler
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.Provides

interface EmailNotificationInjector {
    fun inject(target: EmailNotificationHandler)
}

@DependencyGraph(
    scope = AppScope::class,
    bindingContainers = [
        EmailNotificationModule::class,
        CalendarModule::class,
        AwsModule::class,
        NotifierModule::class
    ]
)
interface EmailNotificationGraph : EmailNotificationInjector {
    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides @Named("configName") configName: String): EmailNotificationGraph
    }
}
