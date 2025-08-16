package com.parmet.squashlambdas

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        EmailNotificationModule::class,
        AwsModule::class,
    ]
)
interface EmailNotificationComponent {
    fun inject(target: EmailNotificationHandler)
}
