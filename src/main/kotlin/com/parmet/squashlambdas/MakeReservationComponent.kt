package com.parmet.squashlambdas

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        MakeReservationModule::class,
        AwsModule::class,
        ClubLockerModule::class,
    ]
)
interface MakeReservationComponent {
    fun inject(target: MakeReservationHandler)
}
