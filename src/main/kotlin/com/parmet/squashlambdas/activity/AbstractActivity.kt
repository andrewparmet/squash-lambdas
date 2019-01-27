package com.parmet.squashlambdas.activity

import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import java.time.Instant

internal abstract class AbstractActivity : Activity {
    private val createTime = Instant.now()

    abstract val start: Instant
    abstract val end: Instant
    abstract val court: Court

    override fun searchString() = "$start $end $court"

    override fun toEvent() =
        Event()
            .setStart(EventDateTime().setDateTime(DateTime(start.toEpochMilli())))
            .setEnd(EventDateTime().setDateTime(DateTime(end.toEpochMilli())))
            .setLocation("$court, Tennis and Racquet Club")
            .setSummary(summary())
            .setDescription(toString())

    protected abstract fun summary(): String
}
