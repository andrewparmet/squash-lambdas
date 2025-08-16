package com.parmet.squashlambdas

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.parmet.squashlambdas.Context.addToContext
import com.parmet.squashlambdas.Context.withInput
import com.parmet.squashlambdas.activity.Sport
import com.parmet.squashlambdas.clublocker.COURTS_BY_ID
import com.parmet.squashlambdas.clublocker.ClubLockerClient
import com.parmet.squashlambdas.clublocker.Slot
import com.parmet.squashlambdas.dagger.DaggerMonitorSlotsComponent
import com.parmet.squashlambdas.dagger.MonitorSlotsComponent
import com.parmet.squashlambdas.monitor.SlotStorageManagerImpl
import com.parmet.squashlambdas.monitor.SlotsTracker
import com.parmet.squashlambdas.notify.Notifier
import com.parmet.squashlambdas.util.inBoston
import io.github.oshai.kotlinlogging.KotlinLogging
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Named

open class MonitorSlotsHandler : RequestHandler<Any, Any> {
    private val logger = KotlinLogging.logger { }

    @Inject
    lateinit var config: MonitorSlotsConfig

    @Inject
    @Named("myNotifier")
    lateinit var myNotifier: Notifier

    @Inject
    @Named("publicNotifier")
    lateinit var publicNotifier: Notifier

    @Inject
    lateinit var dynamoDb: DynamoDbClient

    @Inject
    lateinit var client: ClubLockerClient

    private lateinit var slotsTracker: SlotsTracker

    open fun buildComponent(): MonitorSlotsComponent =
        DaggerMonitorSlotsComponent
            .builder()
            .configName("production-monitor-slots-handler.yml")
            .build()

    fun init() {
        buildComponent().inject(this)

        try {
            client.init()
        } catch (t: Throwable) {
            myNotifier.publishFailedSlotMonitoring(t)
            throw t
        }

        slotsTracker = SlotsTracker(client, SlotStorageManagerImpl(dynamoDb, config.dynamoDb.squashSlotsTableName))
    }

    override fun handleRequest(input: Any, context: Context) {
        init()
        withInput(myNotifier::publishFailedSlotMonitoring, input) { doHandleRequest() }
    }

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
            .filter { COURTS_BY_ID.getValue(it.court).sport in setOf(Sport.Squash, Sport.Tennis) }
            .let {
                addToContext("filteredSlots", it)
                if (it.isNotEmpty()) {
                    publicNotifier.publishFoundOpenSlot(it)
                }
            }
    }
}
