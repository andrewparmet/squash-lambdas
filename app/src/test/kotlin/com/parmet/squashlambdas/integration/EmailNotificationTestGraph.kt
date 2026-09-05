package com.parmet.squashlambdas.integration

import com.parmet.squashlambdas.aws.ObjectStorage
import com.parmet.squashlambdas.aws.TopicPublisher
import com.parmet.squashlambdas.cal.CalendarProvider
import com.parmet.squashlambdas.di.EmailNotificationInjector
import com.parmet.squashlambdas.di.EmailNotificationModule
import com.parmet.squashlambdas.di.NotifierModule
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.Provides

@DependencyGraph(
    scope = AppScope::class,
    bindingContainers = [EmailNotificationModule::class, NotifierModule::class]
)
interface EmailNotificationTestGraph : EmailNotificationInjector {
    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides @Named("configName") configName: String,
            @Provides calendarProvider: CalendarProvider,
            @Provides objectStorage: ObjectStorage,
            @Provides topicPublisher: TopicPublisher,
        ): EmailNotificationTestGraph
    }
}
