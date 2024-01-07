package com.parmet.squashlambdas.email

import biweekly.ICalendar

internal object CalendarExtractor : AbstractMimeMessageExtractor<ICalendar>(::AppendableList) {
    override fun parsers() =
        listOf(MimeParser.TEXT_CALENDAR)
}
