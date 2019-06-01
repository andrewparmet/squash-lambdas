package com.parmet.squashlambdas

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.parmet.squashlambdas.activity.Sport
import com.parmet.squashlambdas.clublocker.COURTS_BY_ID
import com.parmet.squashlambdas.notify.Notifier
import com.parmet.squashlambdas.clublocker.Slot
import com.parmet.squashlambdas.monitor.SlotStorageManagerImpl
import com.parmet.squashlambdas.monitor.SlotsTracker
import mu.KotlinLogging
import org.apache.commons.configuration2.Configuration
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.ConcurrentSkipListMap

class MonitorSlotsHandler : RequestHandler<Any, Any> {
    private val logger = KotlinLogging.logger { }

    private val config: Configuration
    private val notifier: Notifier
    private val slotsTracker: SlotsTracker

    init {
        config = loadConfiguration(System.getenv("CONFIG_NAME") + ".xml")
        val s3 = configureS3()
        val dynamoDb = configureDynamoDb()
        notifier = configureNotifier(config)

        val client = try {
            val clientAndPlayer = configureClubLockerClient(config, s3)
            clientAndPlayer.first.apply { startAsync().awaitRunning() }
        } catch (t: Throwable) {
            notifier.publishFailedSlotMonitoring(t, context)
            throw t
        }

        slotsTracker =
            SlotsTracker(client, SlotStorageManagerImpl(dynamoDb, config.getString("aws.dynamo.squashSlotsTableName")))
    }

    override fun handleRequest(input: Any, context: Context): Any {
        logger.info { "Starting handling of $input" }
        addToContext("input", input)

        return try {
            doHandleRequest()
        } catch (ex: Exception) {
            notifier.publishFailedSlotMonitoring(ex, MonitorSlotsHandler.context)
        } finally {
            MonitorSlotsHandler.context.clear()
        }
    }

    private fun doHandleRequest(): Any {
        val now = Instant.now().inBoston()

        val date = if (now.toLocalTime().isAfter(LocalTime.of(18, 0))) {
            now.plusDays(1)
        } else {
            now
        }.toLocalDate()

        return (0L..1).flatMap {
            checkForDate(date.plusDays(it))
        }.also {
            publish(it)
        }
    }

    private fun checkForDate(date: LocalDate): List<Slot> {
        if (date.dayOfWeek !in MONDAY..FRIDAY) {
            logger.info { "Not checking a weekend" }
            return emptyList()
        }

        addToContext("checkDate", date)

        val newlyOpen = slotsTracker.findNewlyOpen(date)

        if (newlyOpen.isEmpty()) {
            logger.info { "Did not find any newly open slots" }
        } else {
            logger.info { "Found newly open slots: $newlyOpen" }
        }

        return newlyOpen
    }

    private fun publish(slots: List<Slot>) {
        addToContext("foundSlots", slots)
        slots
            .filter { it.startTime in 1701..2099 }
            .filter { COURTS_BY_ID.getValue(it.court).sport == Sport.Squash }
            .let {
                addToContext("filteredSlots", it)
                if (it.isNotEmpty()) {
                    notifier.publishFoundOpenSlot(it)
                }
            }
    }

    companion object {
        val context = ConcurrentSkipListMap<String, Any>()

        fun addToContext(key: String, value: Any?) {
            context[key] = value ?: "Value for key $key was null!"
        }
    }
}
