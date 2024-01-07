package com.parmet.squashlambdas.cal

import com.google.api.services.calendar.Calendar
import com.google.common.collect.Iterables
import com.parmet.squashlambdas.activity.Activity
import mu.KotlinLogging

interface EventManager {
    fun create(activity: Activity)

    fun update(activity: Activity)

    fun delete(activity: Activity)
}

class EventManagerImpl(
    private val calendar: Calendar,
    private val calendarId: String
) : EventManager {
    private val logger = KotlinLogging.logger { }

    override fun create(activity: Activity) {
        logger.info { "Creating activity $activity" }
        calendar.events().insert(calendarId, activity.toEvent()).execute()
    }

    override fun update(activity: Activity) {
        logger.info { "Updating activity $activity" }

        val events = findEvents(activity)

        if (events.size > 1) {
            logger.info { "Found too many events to update; deleting them all. $events" }
            events.forEach {
                calendar.events().delete(calendarId, it.id).execute()
            }
            create(activity)
        } else {
            val event = Iterables.getOnlyElement(events)
            logger.info { "Found one event to update: $event" }
            calendar.events().patch(calendarId, event.id, activity.toEvent()).execute()
        }
    }

    override fun delete(activity: Activity) {
        logger.info { "Deleting activity $activity" }
        findEvents(activity).forEach {
            logger.info { "Deleting event $it" }
            calendar.events().delete(calendarId, it.id).execute()
        }
    }

    private fun findEvents(activity: Activity) =
        calendar.events().list(calendarId)
            .setQ(activity.searchString())
            .execute()
            .items
}
