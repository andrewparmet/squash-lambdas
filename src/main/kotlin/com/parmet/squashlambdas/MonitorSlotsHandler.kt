package com.parmet.squashlambdas

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.parmet.squashlambdas.Context.addToContext
import com.parmet.squashlambdas.Context.withInput
import com.parmet.squashlambdas.activity.Sport
import com.parmet.squashlambdas.clublocker.COURTS_BY_ID
import com.parmet.squashlambdas.clublocker.Slot
import com.parmet.squashlambdas.monitor.SlotStorageManagerImpl
import com.parmet.squashlambdas.monitor.SlotsTracker
import com.parmet.squashlambdas.notify.Notifier
import com.parmet.squashlambdas.util.inBoston
import mu.KotlinLogging
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class MonitorSlotsHandler : RequestHandler<Any, Any> {
    private val logger = KotlinLogging.logger { }

    private val config = loadConfiguration(System.getenv("CONFIG_NAME") + ".xml")
    private val myNotifier: Notifier
    private val publicNotifier: Notifier
    private val slotsTracker: SlotsTracker

    init {
        val s3 = configureS3()
        val dynamoDb = configureDynamoDb()
        myNotifier = configureNotifier(config.getString("aws.sns.myTopicArn"))
        publicNotifier = configureNotifier(config.getString("aws.sns.publicTopicArn"))

        val client =
            try {
                val (client, _) = configureClubLockerClient(config, s3)
                client.apply { startAsync().awaitRunning() }
            } catch (t: Throwable) {
                myNotifier.publishFailedSlotMonitoring(t)
                throw t
            }

        slotsTracker =
            SlotsTracker(client, SlotStorageManagerImpl(dynamoDb, config.getString("aws.dynamo.squashSlotsTableName")))
    }

    override fun handleRequest(input: Any, context: Context) =
        withInput(myNotifier::publishFailedSlotMonitoring, input) { doHandleRequest() }

    private fun doHandleRequest() {
        val now = Instant.now().inBoston()

        val date =
            if (now.toLocalTime().isAfter(LocalTime.of(18, 0))) {
                now.plusDays(1)
            } else {
                now
            }.toLocalDate()

        (0L..1).flatMap {
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
                    publicNotifier.publishFoundOpenSlot(it)
                }
            }
    }
}
