package com.parmet.squashlambdas.reserve

import com.parmet.squashlambdas.BOSTON
import com.parmet.squashlambdas.activity.Court
import com.parmet.squashlambdas.activity.Court.Court1
import com.parmet.squashlambdas.activity.Court.Court2
import com.parmet.squashlambdas.activity.Court.Court3
import com.parmet.squashlambdas.activity.Match
import com.parmet.squashlambdas.activity.Player
import mu.KotlinLogging
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

internal class ReservationMaker(
    private val client: ClubLockerClient,
    private val options: Options
) {
    private val logger = KotlinLogging.logger { }

    fun makeReservation(date: LocalDate, otherPlayer: Player? = null): Result {
        val failures = mutableListOf<ReservationResp.Error>()

        options.courts.forEach { court ->
            options.startTimes.forEach { time ->
                val resp = attemptReservation(date, court, time, otherPlayer)
                when (resp) {
                    is ReservationResp.Success -> {
                        logger.info {
                            "Made reservation on $court at $time${if (otherPlayer != null) " with $otherPlayer" else ""}"
                        }
                        return Result.Success(court, LocalDateTime.of(date, time), otherPlayer, failures)
                    }

                    is ReservationResp.Error -> {
                        logger.info { "Failed to make reservation on $court at $time: $resp" }
                        failures.add(resp)
                        if (resp.isFatal()) {
                            logger.info { "Encountered non-retryable error, terminating" }
                            return Result.Failure(date, failures)
                        }
                    }
                }
            }
        }
        return Result.Failure(date, failures)
    }

    private fun attemptReservation(date: LocalDate, court: Court, startTime: LocalTime, player: Player?): ReservationResp {
        val localDateTime = LocalDateTime.of(date, startTime)
        val start = localDateTime.toInstant(BOSTON.rules.getOffset(localDateTime))
        return client.makeReservation(
            Match(
                court,
                start,
                start.plus(Duration.ofMinutes(45)),
                if (player == null) {
                    setOf(options.hostPlayer)
                } else {
                    setOf(options.hostPlayer, player)
                }))
    }

    private fun ReservationResp.Error.isFatal(): Boolean {
        return message.contains("Player has already booked their maximum number of prime time reservations")
    }

    internal class Options(
        val hostPlayer: Player,
        val courts: List<Court> = listOf(Court2, Court1, Court3),
        val startTimes: List<LocalTime> = listOf(LocalTime.of(18, 0), LocalTime.of(18, 45), LocalTime.of(19, 30))
    )

    internal sealed class Result {
        data class Success(
            val court: Court,
            val time: LocalDateTime,
            val player: Player?,
            val failures: List<ReservationResp.Error>
        ) : Result()

        data class Failure(
            val date: LocalDate,
            val failures: List<ReservationResp.Error>
        ) : Result()
    }
}
