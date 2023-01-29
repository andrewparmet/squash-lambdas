package com.parmet.squashlambdas

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.s3.AmazonS3
import com.parmet.squashlambdas.Context.addToContext
import com.parmet.squashlambdas.Context.withInput
import com.parmet.squashlambdas.activity.Player
import com.parmet.squashlambdas.clublocker.ClubLockerClient
import com.parmet.squashlambdas.notify.Notifier
import com.parmet.squashlambdas.reserve.InputParser
import com.parmet.squashlambdas.reserve.ReservationMaker
import com.parmet.squashlambdas.reserve.ReservationMaker.Result
import com.parmet.squashlambdas.reserve.TimeFilter
import mu.KotlinLogging
import org.apache.commons.configuration2.Configuration
import java.time.LocalDate

class MakeReservationHandler : RequestHandler<Any, Any> {
    private val logger = KotlinLogging.logger { }

    private val config: Configuration
    private val s3: AmazonS3
    private val notifier: Notifier
    private val client: ClubLockerClient
    private val hostPlayer: Player

    init {
        config = loadConfiguration(System.getenv("CONFIG_NAME") + ".xml")
        s3 = configureS3()
        notifier = configureNotifier(config)
        try {
            val clientAndPlayer = configureClubLockerClient(config, s3)
            client = clientAndPlayer.first.apply { startAsync().awaitRunning() }
            hostPlayer = clientAndPlayer.second
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
                    getPreferredTimes(config, s3)
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
