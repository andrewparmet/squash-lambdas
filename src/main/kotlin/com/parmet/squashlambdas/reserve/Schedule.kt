package com.parmet.squashlambdas.reserve

import java.time.DayOfWeek
import java.time.LocalDate

class Schedule(
    private val days: Collection<DayOfWeek>
) {
    fun shouldMakeReservation(date: LocalDate): Boolean =
        days.contains(date.dayOfWeek)

    companion object {
        fun fromString(schedule: String): Schedule =
            Schedule(
                schedule.mapNonEmptyLines {
                    DayOfWeek.valueOf(it.uppercase())
                }
            )
    }
}
