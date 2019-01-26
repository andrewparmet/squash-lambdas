package com.parmet.squashlambdas.activity

import com.google.common.base.Preconditions.checkArgument
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

object TimeParser {
    /** "Date: Thursday, June 28th 2018 Time: 08:15 PM to 09:00 PM". */
    private val PATTERN =
        Pattern.compile(".* Date: .*, (.*) (\\d\\d?).* (.*) Time: (.*) to (.*) (AM|PM).*")

    private val TIME = DateTimeFormatter.ofPattern("hh:mm a")

    private val BOSTON = ZoneId.of("America/New_York")

    fun parse(body: String): StartAndEnd {
        val matcher = PATTERN.matcher(body)
        checkArgument(matcher.matches(), "Unable to find start and end from body %s", body)
        val month = Month.valueOf(matcher.group(1).toUpperCase())
        val dayOfMonth = Integer.parseInt(matcher.group(2))
        val year = Integer.parseInt(matcher.group(3))
        val start = LocalTime.parse(matcher.group(4), TIME)
        val end = LocalTime.parse(matcher.group(5) + " " + matcher.group(6), TIME)

        val date = LocalDate.of(year, month, dayOfMonth)

        return StartAndEnd(inBoston(date, start), inBoston(date, end))
    }

    private fun inBoston(date: LocalDate, time: LocalTime) =
        LocalDateTime.of(date, time).atZone(BOSTON).toInstant()
}
