package com.parmet.squashlambdas.reserve

import java.io.InputStream
import java.time.DayOfWeek
import java.time.LocalDate

class Schedule(
    private val days: Collection<DayOfWeek>
) {
    fun shouldMakeReservation(date: LocalDate): Boolean =
        days.contains(date.dayOfWeek)

    companion object {
        fun fromStream(stream: InputStream): Schedule =
            Schedule(
                stream.mapNonEmptyLines {
                    DayOfWeek.valueOf(it.uppercase())
                }.toList()
            )
    }
}
