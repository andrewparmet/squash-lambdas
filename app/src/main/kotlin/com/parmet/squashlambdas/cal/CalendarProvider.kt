package com.parmet.squashlambdas.cal

import com.google.api.services.calendar.Calendar
import com.parmet.squashlambdas.GoogleCalConfig
import com.parmet.squashlambdas.configureCalendar
import com.parmet.squashlambdas.util.FileLoader
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

interface CalendarProvider {
    suspend fun get(): Calendar
}

@ContributesBinding(AppScope::class)
@Inject
@SingleIn(AppScope::class)
class GoogleCalendarProvider(
    private val config: GoogleCalConfig,
    private val fileLoader: FileLoader
) : CalendarProvider {
    private var calendar: Calendar? = null

    override suspend fun get(): Calendar =
        calendar ?: configureCalendar(config, fileLoader).also { calendar = it }
}
