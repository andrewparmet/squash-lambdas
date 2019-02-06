package com.parmet.squashlambdas.reserve

import com.parmet.squashlambdas.BOSTON
import mu.KotlinLogging
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime

internal class TimeFilter(
    private val requestDate: LocalDate,
    private val clock: Clock
) {
    private val logger = KotlinLogging.logger { }

    constructor(requestDate: LocalDate): this(requestDate, Clock.systemUTC())

    fun filterBasedOnBostonTime(): Output {
        val time = Instant.now(clock).atZone(BOSTON)
        logger.info { "Request date: $requestDate; Time in Boston: $time" }

        return Output(
            when {
                dateIsMoreThanSevenDaysInFuture(time) ->
                    "Date $requestDate is too far in the future to reserve now ($time)"
                reservationShouldAlreadyHaveBeenMade(time) ->
                    "Not scheduling past 1 am; assuming that the 12 am run already ran ($time)"
                else -> null
            }
        )
    }

    private fun dateIsMoreThanSevenDaysInFuture(time: ZonedDateTime): Boolean {
        val dateInBoston = time.toLocalDate()
        val tooSoonToMakeReservation = requestDate > dateInBoston.plusDays(7)
        logger.info {
            "Checking request date $requestDate against date in Boston $dateInBoston; " +
                "Too soon to make a reservation? $tooSoonToMakeReservation" }
        return tooSoonToMakeReservation
    }

    private fun reservationShouldAlreadyHaveBeenMade(time: ZonedDateTime): Boolean {
        val pastOneAm = time.hour >= 1
        logger.info { "Checking hour for time $time: Past 1 am? $pastOneAm" }
        return pastOneAm
    }

    internal class Output(val reason: String?) {
        fun shouldMakeReservation() = reason == null
    }
}
