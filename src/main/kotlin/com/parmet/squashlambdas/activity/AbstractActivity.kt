package com.parmet.squashlambdas.activity

import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import java.time.Instant

abstract class AbstractActivity(
    @Transient
    private val start: Instant,
    @Transient
    private val end: Instant,
    @Transient
    private val court: Court
) : Activity {
    private val createTime = Instant.now()

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
