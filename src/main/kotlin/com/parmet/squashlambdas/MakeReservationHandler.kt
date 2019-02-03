package com.parmet.squashlambdas

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.parmet.squashlambdas.activity.Player
import com.parmet.squashlambdas.notify.Notifier
import com.parmet.squashlambdas.reserve.ClubLockerClient
import com.parmet.squashlambdas.reserve.InputParser
import com.parmet.squashlambdas.reserve.ReservationMaker
import com.parmet.squashlambdas.reserve.ReservationMaker.Result
import com.parmet.squashlambdas.reserve.Schedule
import mu.KotlinLogging
import java.time.LocalDate
import java.util.concurrent.ConcurrentSkipListMap

class MakeReservationHandler : RequestHandler<Any, Any> {
    private val logger = KotlinLogging.logger { }

    private val notifier: Notifier
    private val client: ClubLockerClient
    private val hostPlayer: Player
    private val schedule: Schedule

    init {
        val config = loadConfiguration(System.getenv("CONFIG_NAME") + ".xml")
        val s3 = configureS3()
        notifier = Notifier(configureSns(), config.getString("aws.sns.handledTopicArn"))
        try {
            val clientAndPlayer = configureClubLockerClient(config, s3)
            client = clientAndPlayer.first.apply { startAsync().awaitRunning() }
            hostPlayer = clientAndPlayer.second
            schedule = getSchedule(config, s3)
        } catch (t: Throwable) {
            notifier.publishFailedReservation(t, context)
            throw t
        }
    }

    override fun handleRequest(input: Any, ignore: Context): Any {
        return try {
            addToContext("input", input)

            val requestDate = InputParser.parseRequestDate(input)
            addToContext("requestDate", requestDate)

            if (schedule.shouldMakeReservation(requestDate)) {
                makeReservation(requestDate)
            } else {
                "No reservation requested for $requestDate"
            }
        } catch (t: Throwable) {
            notifier.publishFailedReservation(t, context)
            throw t
        }
    }

    private fun makeReservation(requestDate: LocalDate): Any {
        logger.info { "Attempting to book a reservation for $requestDate" }

        val result =
            ReservationMaker(client, ReservationMaker.Options(hostPlayer))
                .makeReservation(requestDate)

        addToContext("result", result)

        when (result) {
            is Result.Success -> notifier.publishSuccessfulReservation(result)
            is Result.Failure -> notifier.publishFailedReservation(result, context)
        }

        return result
    }

    companion object {
        private val context = ConcurrentSkipListMap<String, Any>()

        fun addToContext(key: String, value: Any?) {
            context[key] = value ?: "Value for key $key was null!"
        }
    }
}
