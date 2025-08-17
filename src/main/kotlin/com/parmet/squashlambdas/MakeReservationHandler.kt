package com.parmet.squashlambdas

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent
import com.parmet.squashlambdas.Context.addToContext
import com.parmet.squashlambdas.activity.Player
import com.parmet.squashlambdas.clublocker.ClubLockerClient
import com.parmet.squashlambdas.dagger.DaggerMakeReservationComponent
import com.parmet.squashlambdas.dagger.MakeReservationComponent
import com.parmet.squashlambdas.notify.Notifier
import com.parmet.squashlambdas.reserve.ReservationMaker
import com.parmet.squashlambdas.reserve.ReservationMaker.Result
import com.parmet.squashlambdas.reserve.TimeFilter
import com.parmet.squashlambdas.util.HasNotifier
import com.parmet.squashlambdas.util.withErrorHandling
import io.github.oshai.kotlinlogging.KotlinLogging
import software.amazon.awssdk.services.s3.S3Client
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Named

private val logger = KotlinLogging.logger { }

open class MakeReservationHandler :
    RequestHandler<ScheduledEvent, Any>,
    HasNotifier {

    @Inject
    lateinit var config: MakeReservationConfig

    @Inject
    lateinit var s3: S3Client

    @Inject
    @Named("myNotifier")
    override lateinit var notifier: Notifier

    @Inject
    lateinit var client: ClubLockerClient

    @Inject
    lateinit var hostPlayer: Player

    open fun buildComponent(): MakeReservationComponent =
        DaggerMakeReservationComponent
            .builder()
            .configName("production-make-reservation-handler.yml")
            .build()

    final override fun handleRequest(input: ScheduledEvent, context: Context) {
        withErrorHandling(input) {
            buildComponent().inject(this)
            doHandleRequest(input).also { logger.info { "Returning result: $it" } }
        }
    }

    private fun doHandleRequest(input: ScheduledEvent) {
        val timeFilter = TimeFilter(input.time)
        val requestDate = timeFilter.requestDate

        addToContext("input", input)
        addToContext("requestDate", requestDate)

        val reservationTimeFiltered = timeFilter.filterBasedOnBostonTime()
        if (!reservationTimeFiltered.shouldMakeReservation()) {
            logger.info { "Not making a reservation: ${reservationTimeFiltered.reason}" }
            reservationTimeFiltered.reason!!
        } else {
            processSchedule(requestDate)
        }
    }

    private fun processSchedule(requestDate: LocalDate): Any {
        val schedule = getSchedule(config.schedule, s3)
        return if (schedule.shouldMakeReservation(requestDate)) {
            makeReservation(requestDate)
        } else {
            logger.info { "No reservation requested for $requestDate ($schedule)" }
            "No reservation requested for $requestDate"
        }
    }

    private fun makeReservation(requestDate: LocalDate): Any {
        logger.info { "Attempting to book a reservation for $requestDate" }

        val result =
            ReservationMaker(
                client,
                ReservationMaker.Options(
                    hostPlayer,
                    getPreferredCourts(config.courts, s3),
                    getPreferredTimes(config.times, s3),
                )
            ).makeReservation(requestDate)

        addToContext("result", result)

        when (result) {
            is Result.Success -> notifier.publishSuccessfulReservation(result)
            is Result.Failure -> error("error making reservation: $result")
        }

        return result
    }
}
