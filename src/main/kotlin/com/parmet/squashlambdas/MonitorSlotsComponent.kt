package com.parmet.squashlambdas

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        MonitorSlotsModule::class,
        AwsModule::class,
        ClubLockerModule::class,
    ]
)
interface MonitorSlotsComponent {
    fun inject(target: MonitorSlotsHandler)
}
