package com.parmet.squashlambdas

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent
import com.parmet.squashlambdas.Context.addToContext
import com.parmet.squashlambdas.activity.Sport
import com.parmet.squashlambdas.clublocker.COURTS_BY_ID
import com.parmet.squashlambdas.clublocker.Slot
import com.parmet.squashlambdas.clublocker.TokenStatusManager
import com.parmet.squashlambdas.di.MonitorSlotsGraph
import com.parmet.squashlambdas.monitor.SlotsTracker
import com.parmet.squashlambdas.notify.Notifier
import com.parmet.squashlambdas.util.HasNotifier
import com.parmet.squashlambdas.util.inBoston
import com.parmet.squashlambdas.util.withErrorHandling
import dev.zacsweers.metro.HasMemberInjections
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.createGraphFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

private val logger = KotlinLogging.logger { }

@HasMemberInjections
open class MonitorSlotsHandler :
    RequestHandler<ScheduledEvent, Any>,
    HasNotifier {

    @Inject
    @Named("myNotifier")
    override lateinit var notifier: Notifier

    @Inject
    @Named("publicNotifier")
    lateinit var publicNotifier: Notifier

    @Inject
    lateinit var slotsTracker: SlotsTracker

    @Inject
    lateinit var tokenStatusManager: TokenStatusManager

    private val graph by lazy { buildGraph() }

    private fun buildGraph(): MonitorSlotsGraph =
        createGraphFactory<MonitorSlotsGraph.Factory>()
            .create("production-monitor-slots-handler.yml")

    final override fun handleRequest(input: ScheduledEvent, context: Context) {
        withErrorHandling(input) {
            graph.inject(this)
            doHandleRequest()
        }
    }

    private fun doHandleRequest() {
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
