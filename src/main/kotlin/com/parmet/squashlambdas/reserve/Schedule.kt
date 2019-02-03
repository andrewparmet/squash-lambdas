package com.parmet.squashlambdas.reserve

import com.amazonaws.services.s3.AmazonS3
import mu.KotlinLogging
import java.time.DayOfWeek
import java.time.LocalDate

internal class Schedule(
    private val days: Collection<DayOfWeek>
) {
    fun shouldMakeReservation(date: LocalDate): Boolean {
        return days.contains(date.dayOfWeek)
    }

    companion object {
        private val logger = KotlinLogging.logger { }

        fun fromS3(s3: AmazonS3, bucket: String, key: String): Schedule {
            val raw = s3.getObjectAsString(bucket, key)
            logger.info { "Retrieved schedule: $raw" }
            return fromString(raw)
        }

        fun fromString(schedule: String): Schedule {
            return Schedule(
                schedule.lines()
                    .filter { it.isNotBlank() }
                    .map { DayOfWeek.valueOf(it.toUpperCase()) }
            )
        }
    }
}
