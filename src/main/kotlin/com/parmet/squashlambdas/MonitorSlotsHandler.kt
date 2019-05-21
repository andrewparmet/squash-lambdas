package com.parmet.squashlambdas

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.s3.AmazonS3
import com.parmet.squashlambdas.activity.Player
import com.parmet.squashlambdas.activity.Sport
import com.parmet.squashlambdas.clublocker.COURTS_BY_ID
import com.parmet.squashlambdas.notify.Notifier
import com.parmet.squashlambdas.clublocker.ClubLockerClient
import com.parmet.squashlambdas.clublocker.Slot
import com.parmet.squashlambdas.monitor.SlotStorageManagerImpl
import com.parmet.squashlambdas.monitor.SlotsTracker
import mu.KotlinLogging
import org.apache.commons.configuration2.Configuration
import java.time.Instant
import java.time.LocalTime
import java.util.concurrent.ConcurrentSkipListMap

class MonitorSlotsHandler : RequestHandler<Any, Any> {
    private val logger = KotlinLogging.logger { }

    private val config: Configuration
    private val s3: AmazonS3
    private val dynamoDb: AmazonDynamoDB
    private val notifier: Notifier
    private val client: ClubLockerClient
    private val hostPlayer: Player

    init {
        config = loadConfiguration(System.getenv("CONFIG_NAME") + ".xml")
        s3 = configureS3()
        dynamoDb = configureDynamoDb()
        notifier = Notifier(configureSns(), config.getString("aws.sns.handledTopicArn"))
        try {
            val clientAndPlayer = configureClubLockerClient(config, s3)
            client = clientAndPlayer.first.apply { startAsync().awaitRunning() }
            hostPlayer = clientAndPlayer.second
        } catch (t: Throwable) {
            notifier.publishFailedReservation(t, context)
            throw t
        }
    }

    override fun handleRequest(input: Any, context: Context): Any {
        logger.info { "Starting handling of $input" }
        addToContext("input", input)

        return try {
            doHandleRequest()
        } catch (ex: Exception) {
            notifier.publishFailedSlotMonitoring(ex, MonitorSlotsHandler.context)
        }
    }

    private fun doHandleRequest(): Any {
        val dynamoClient = SlotStorageManagerImpl(
            dynamoDb,
            config.getString("aws.dynamo.squashSlotsTableName")
        )

        val now = Instant.now().inBoston()

        val date = if (now.toLocalTime().isAfter(LocalTime.of(18, 0))) {
            now.toLocalDate().plusDays(1)
        } else {
            now.toLocalDate()
        }

        addToContext("checkDate", date)

        val newlyOpen = SlotsTracker(client, dynamoClient).findNewlyOpen(date)

        if (newlyOpen.isEmpty()) {
            logger.info { "Did not find any newly open slots" }
        } else {
            logger.info { "Found newly open slots: $newlyOpen" }
            publish(newlyOpen)
        }

        return newlyOpen
    }

    private fun publish(slots: List<Slot>) {
        addToContext("foundSlots", slots)
        slots
            .filter { it.startTime in 501..799 }
            .filter { COURTS_BY_ID.getValue(it.court).sport == Sport.Squash }
            .let {
                addToContext("filteredSlots", it)
                if (it.isNotEmpty()) {
                    notifier.publishFoundOpenSlot(it)
                }
            }
    }

    companion object {
        private val context = ConcurrentSkipListMap<String, Any>()

        fun addToContext(key: String, value: Any?) {
            context[key] = value ?: "Value for key $key was null!"
        }
    }
}
