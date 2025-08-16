package com.parmet.squashlambdas

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.google.inject.Guice
import com.google.inject.Inject
import com.google.inject.name.Named
import com.parmet.squashlambdas.Context.addToContext
import com.parmet.squashlambdas.Context.withInput
import com.parmet.squashlambdas.activity.Player
import com.parmet.squashlambdas.clublocker.ClubLockerClient
import com.parmet.squashlambdas.notify.Notifier
import com.parmet.squashlambdas.reserve.InputParser
import com.parmet.squashlambdas.reserve.ReservationMaker
import com.parmet.squashlambdas.reserve.ReservationMaker.Result
import com.parmet.squashlambdas.reserve.TimeFilter
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.LocalDate
import software.amazon.awssdk.services.s3.S3Client

class MakeReservationHandler : RequestHandler<Any, Any> {
    private val logger = KotlinLogging.logger { }

    @Inject
    private lateinit var config: AppConfig

    @Inject
    private lateinit var s3: S3Client

    @Inject
    @Named("myNotifier")
    private lateinit var notifier: Notifier

    @Inject
    private lateinit var client: ClubLockerClient

    @Inject
    private lateinit var hostPlayer: Player

    init {
        val injector = Guice.createInjector(ConfigModule())
        injector.injectMembers(this)
        try {
            client.startAsync().awaitRunning()
        } catch (t: Throwable) {
            notifier.publishFailedReservation(t)
            throw t
        }
    }

    override fun handleRequest(input: Any, context: Context) =
        withInput(notifier::publishFailedReservation, input) {
            doHandleRequest(input).also { logger.info { "Returning result: $it" } }
        }

    private fun doHandleRequest(input: Any) {
        val requestDate = InputParser.parseRequestDate(input)
        addToContext("requestDate", requestDate)

        val timeFiltered = TimeFilter(requestDate).filterBasedOnBostonTime()
        if (!timeFiltered.shouldMakeReservation()) {
            logger.info { "Not making a reservation: ${timeFiltered.reason}" }
            timeFiltered.reason!!
        } else {
            processSchedule(requestDate)
        }
    }

    private fun processSchedule(requestDate: LocalDate): Any {
        val schedule = getSchedule(config, s3)
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
                    getPreferredCourts(config, s3),
                    getPreferredTimes(config, s3),
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
