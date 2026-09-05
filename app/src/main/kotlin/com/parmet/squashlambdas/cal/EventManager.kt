package com.parmet.squashlambdas.cal

import com.parmet.squashlambdas.GoogleCalConfig
import com.parmet.squashlambdas.activity.Activity
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Inject
class EventManager(
    private val calendarProvider: CalendarProvider,
    config: GoogleCalConfig
) {
    private val logger = KotlinLogging.logger { }
    private val calendarId = config.calendarId

    suspend fun create(activity: Activity) {
        logger.info { "Creating activity $activity" }
        withCalendar<Unit> {
            it.events().insert(calendarId, activity.toEvent()).execute()
        }
    }

    suspend fun update(activity: Activity) {
        logger.info { "Updating activity $activity" }

        val events = findEvents(activity)

        when (events.size) {
            0 -> create(activity)

            1 -> {
                val event = events.single()
                logger.info { "Found one event to update: $event" }
                withCalendar<Unit> {
                    it.events().patch(calendarId, event.id, activity.toEvent()).execute()
                }
            }

            else -> {
                logger.info { "Found too many events to update; deleting them all. $events" }
                events.forEach {
                    withCalendar<Unit> { calendar ->
                        calendar.events().delete(calendarId, it.id).execute()
                    }
                }
                create(activity)
            }
        }
    }

    suspend fun delete(activity: Activity) {
        logger.info { "Deleting activity $activity" }
        findEvents(activity).forEach {
            logger.info { "Deleting event $it" }
            withCalendar<Unit> { calendar ->
                calendar.events().delete(calendarId, it.id).execute()
            }
        }
    }

    private suspend fun findEvents(activity: Activity) =
        withCalendar {
            it.events().list(calendarId)
                .setQ(activity.searchString())
                .execute()
                .items
        }

    private suspend fun <T> withCalendar(block: (com.google.api.services.calendar.Calendar) -> T): T =
        withContext(Dispatchers.IO) { block(calendarProvider.get()) }
}
