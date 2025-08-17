package com.parmet.squashlambdas.dagger

import com.parmet.squashlambdas.MakeReservationHandler
import dagger.BindsInstance
import dagger.Component
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        MakeReservationModule::class,
        AwsModule::class,
        ClubLockerModule::class,
        NotifierModule::class
    ]
)
interface MakeReservationComponent {
    fun inject(target: MakeReservationHandler)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun configName(@Named("configName") configName: String): Builder

        fun build(): MakeReservationComponent
    }
}
