package com.parmet.squashlambdas

import com.amazonaws.services.lambda.runtime.events.ScheduledEvent
import com.parmet.squashlambdas.RequestContext.addToContext
import com.parmet.squashlambdas.activity.Sport
import com.parmet.squashlambdas.clublocker.COURTS_BY_ID
import com.parmet.squashlambdas.clublocker.Slot
import com.parmet.squashlambdas.clublocker.TokenStatusManager
import com.parmet.squashlambdas.di.MonitorSlotsGraph
import com.parmet.squashlambdas.json.Json
import com.parmet.squashlambdas.monitor.SlotsTracker
import com.parmet.squashlambdas.notify.OpenSlotNotifier
import com.parmet.squashlambdas.notify.OperatorNotifier
import com.parmet.squashlambdas.util.SnapStartInitializer
import com.parmet.squashlambdas.util.inBoston
import dev.zacsweers.metro.HasMemberInjections
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.createGraphFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.JsonPrimitive
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

private val logger = KotlinLogging.logger { }

@HasMemberInjections
open class MonitorSlotsHandler : ScheduledLambdaRequestHandler() {

    @Inject
    final override lateinit var notifier: OperatorNotifier

    @Inject
    lateinit var publicNotifier: OpenSlotNotifier

    @Inject
    lateinit var slotsTracker: SlotsTracker

    @Inject
    lateinit var tokenStatusManager: TokenStatusManager

    private val graph by lazy { buildGraph() }
    private val initializer = SnapStartInitializer { graph.inject(this) }

    private fun buildGraph(): MonitorSlotsGraph =
        createGraphFactory<MonitorSlotsGraph.Factory>()
            .create("production-monitor-slots-handler.conf")

    final override fun initialize() {
        initializer.initialize()
    }

    final override suspend fun process(input: ScheduledEvent) {
        if (!tokenStatusManager.isTokenValid()) {
            logger.info { "Token is marked invalid, skipping slot monitoring" }
            return
        }

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

    private suspend fun checkForDate(date: LocalDate): List<Slot> {
        if (date.dayOfWeek !in MONDAY..FRIDAY) {
            logger.info { "Not checking a weekend" }
            return emptyList()
        }

        addToContext("checkDate", JsonPrimitive(date.toString()))

        val newlyOpen = slotsTracker.findNewlyOpen(date)

        if (newlyOpen.isEmpty()) {
            logger.info { "Did not find any newly open slots" }
        } else {
            logger.info { "Found newly open slots: $newlyOpen" }
        }

        return newlyOpen
    }

    private suspend fun publish(slots: List<Slot>) {
        addToContext("foundSlots", Json.element(slots))
        slots
            .filter { it.startTime in 1701..2099 }
            .filter { COURTS_BY_ID.getValue(it.court).sport in setOf(Sport.Squash, Sport.Tennis) }
            .let {
                addToContext("filteredSlots", Json.element(it))
                if (it.isNotEmpty()) {
                    publicNotifier.publishFoundOpenSlot(it)
                }
            }
    }
}
