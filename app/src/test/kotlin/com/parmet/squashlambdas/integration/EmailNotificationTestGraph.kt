package com.parmet.squashlambdas.integration

import com.parmet.squashlambdas.EmailNotificationConfig
import com.parmet.squashlambdas.aws.ObjectStorage
import com.parmet.squashlambdas.aws.TopicPublisher
import com.parmet.squashlambdas.cal.CalendarProvider
import com.parmet.squashlambdas.cal.ChangeSummaryResolver
import com.parmet.squashlambdas.cal.ClubLockerChangeSummaryResolver
import com.parmet.squashlambdas.cal.GoogleCalendarProvider
import com.parmet.squashlambdas.di.EmailNotificationModule
import com.parmet.squashlambdas.di.EmailNotificationProcessorProvider
import com.parmet.squashlambdas.di.NotifierModule
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides

@DependencyGraph(
    scope = AppScope::class,
    excludes = [ClubLockerChangeSummaryResolver::class, GoogleCalendarProvider::class],
    bindingContainers = [EmailNotificationModule::class, NotifierModule::class]
)
interface EmailNotificationTestGraph : EmailNotificationProcessorProvider {
    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides config: EmailNotificationConfig,
            @Provides calendarProvider: CalendarProvider,
            @Provides changeSummaryResolver: ChangeSummaryResolver,
            @Provides objectStorage: ObjectStorage,
            @Provides topicPublisher: TopicPublisher,
        ): EmailNotificationTestGraph
    }
}
