package com.parmet.squashlambdas.di

import com.parmet.squashlambdas.MakeReservationHandler
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.Provides

@DependencyGraph(
    scope = AppScope::class,
    bindingContainers = [
        MakeReservationModule::class,
        AwsModule::class,
        ClubLockerModule::class,
        NotifierModule::class
    ]
)
interface MakeReservationGraph {
    fun inject(target: MakeReservationHandler)

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides @Named("configName") configName: String): MakeReservationGraph
    }
}
