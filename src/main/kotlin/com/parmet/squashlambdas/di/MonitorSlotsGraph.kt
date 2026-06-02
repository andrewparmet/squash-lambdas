package com.parmet.squashlambdas.di

import com.parmet.squashlambdas.MonitorSlotsHandler
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.Provides

@DependencyGraph(
    scope = AppScope::class,
    bindingContainers = [
        MonitorSlotsModule::class,
        AwsModule::class,
        ClubLockerModule::class,
        NotifierModule::class
    ]
)
interface MonitorSlotsGraph {
    fun inject(target: MonitorSlotsHandler)

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides @Named("configName") configName: String): MonitorSlotsGraph
    }
}
