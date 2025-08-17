package com.parmet.squashlambdas.dagger

import com.parmet.squashlambdas.EmailNotificationHandler
import dagger.BindsInstance
import dagger.Component
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        EmailNotificationModule::class,
        CalendarModule::class,
        AwsModule::class,
        NotifierModule::class
    ]
)
interface EmailNotificationComponent {
    fun inject(target: EmailNotificationHandler)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun configName(@Named("configName") configName: String): Builder

        fun build(): EmailNotificationComponent
    }
}
