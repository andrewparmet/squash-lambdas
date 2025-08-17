package com.parmet.squashlambdas.dagger

import com.parmet.squashlambdas.MonitorSlotsHandler
import dagger.BindsInstance
import dagger.Component
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        MonitorSlotsModule::class,
        AwsModule::class,
        ClubLockerModule::class,
        NotifierModule::class
    ]
)
interface MonitorSlotsComponent {
    fun inject(target: MonitorSlotsHandler)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun configName(@Named("configName") configName: String): Builder

        fun build(): MonitorSlotsComponent
    }
}
