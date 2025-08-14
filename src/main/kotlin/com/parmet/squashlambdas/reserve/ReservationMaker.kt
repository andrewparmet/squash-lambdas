package com.parmet.squashlambdas.reserve

import com.parmet.squashlambdas.activity.Court
import com.parmet.squashlambdas.activity.Court.Court1
import com.parmet.squashlambdas.activity.Court.Court2
import com.parmet.squashlambdas.activity.Court.Court3
import com.parmet.squashlambdas.activity.Court.Court5
import com.parmet.squashlambdas.activity.Court.Court6
import com.parmet.squashlambdas.activity.Court.Court7
import com.parmet.squashlambdas.activity.Court.FitnessClasses
import com.parmet.squashlambdas.activity.Court.RacquetsCourt
import com.parmet.squashlambdas.activity.Court.TennisCourt
import com.parmet.squashlambdas.activity.Match
import com.parmet.squashlambdas.activity.Player
import com.parmet.squashlambdas.clublocker.ClubLockerClient
import com.parmet.squashlambdas.clublocker.ReservationResp
import com.parmet.squashlambdas.util.inBoston
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ReservationMaker(
    private val client: ClubLockerClient,
    private val options: Options
) {
    private val logger = KotlinLogging.logger { }

    fun makeReservation(date: LocalDate, otherPlayer: Player? = null): Result {
        val failures = mutableListOf<ReservationResp.NonSuccess>()

        options.startTimes.forEach { time ->
            options.courts.forEach { court ->
                when (val resp = attemptReservation(date, court, time, otherPlayer)) {
                    is ReservationResp.Success -> {
                        logger.info {
                            "Made reservation on $court at $time${if (otherPlayer != null) " with $otherPlayer" else ""}"
                        }
                        return Result.Success(resp.match, failures)
                    }

                    is ReservationResp.Error -> {
                        logger.info { "Failed to make reservation on $court at $time: $resp" }
                        failures.add(resp)
                        if (resp.isFatal()) {
                            logger.info { "Encountered non-retryable error, terminating" }
                            return Result.Failure(date, failures)
                        }
                    }

                    is ReservationResp.Failure -> {
                        logger.info(resp.t) {
                            "Encountered an unknown failure while attempting a reservation for ${resp.match}"
                        }
                        failures.add(resp)
                    }
                }
            }
        }
        return Result.Failure(date, failures)
    }

    private fun attemptReservation(
        date: LocalDate,
        court: Court,
        startTime: LocalTime,
        player: Player?
    ): ReservationResp {
        val localDateTime = LocalDateTime.of(date, startTime)
        val start = localDateTime.inBoston().toInstant()
        return client.makeReservation(
            Match(
                court,
                start,
                start + duration(court),
                "this domain object should probably not be reused this way",
                if (player == null) {
                    setOf(options.hostPlayer)
                } else {
                    setOf(options.hostPlayer, player)
                },
            ),
        )
    }

    private fun duration(court: Court) =
        when (court) {
            Court1, Court2, Court3, Court5, Court6, Court7 -> Duration.ofMinutes(45)
            RacquetsCourt, TennisCourt -> Duration.ofHours(1)
            FitnessClasses -> error("why are you doing this")
        }

    private fun ReservationResp.Error.isFatal(): Boolean =
        (message as? String)?.contains("Player has already booked their maximum number of prime time reservations")
            ?: false

    class Options(
        val hostPlayer: Player,
        val courts: List<Court>,
        val startTimes: List<LocalTime>
    )

    sealed class Result {
        data class Success(
            val match: Match,
            val failures: List<ReservationResp.NonSuccess>
        ) : Result()

        data class Failure(
            val date: LocalDate,
            val failures: List<ReservationResp.NonSuccess>
        ) : Result()
    }
}
