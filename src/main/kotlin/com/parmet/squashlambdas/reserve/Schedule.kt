package com.parmet.squashlambdas.reserve

import java.time.DayOfWeek
import java.time.LocalDate

class Schedule(
    private val days: Collection<DayOfWeek>,
) {
    fun shouldMakeReservation(date: LocalDate): Boolean {
        return days.contains(date.dayOfWeek)
    }

    companion object {
        fun fromString(schedule: String): Schedule {
            return Schedule(
                schedule.mapNonEmptyLines {
                    DayOfWeek.valueOf(it.uppercase())
                },
            )
        }
    }
}
