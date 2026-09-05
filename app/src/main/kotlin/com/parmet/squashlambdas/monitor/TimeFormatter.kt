package com.parmet.squashlambdas.monitor

import java.text.DecimalFormat

private val MINUTES_FORMAT = DecimalFormat("00")

internal object TimeFormatter {
    fun formatTime(time: Int): String {
        val hours = time / 100
        val minutes = time % 100
        val amPmHour = hours % 12

        val am = amPmHour == hours

        return "$amPmHour:${MINUTES_FORMAT.format(minutes)} ${if (am) "am" else "pm"}"
    }
}
