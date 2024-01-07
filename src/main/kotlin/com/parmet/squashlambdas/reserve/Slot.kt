package com.parmet.squashlambdas.reserve

import java.time.LocalTime
import java.time.format.DateTimeFormatter

internal data class Slot(
    val start: LocalTime,
    val end: LocalTime,
)

private val TIME_FORMAT = DateTimeFormatter.ofPattern("HHmm")

private val SLOT_FORMAT = DateTimeFormatter.ofPattern("HH:mm")

internal val Slot.startTime: Int
    get() = TIME_FORMAT.format(start).toInt()

internal val Slot.endTime: Int
    get() = TIME_FORMAT.format(end).toInt()

internal val Slot.slot: String
    get() = "${SLOT_FORMAT.format(start)}-${SLOT_FORMAT.format(end)}"
