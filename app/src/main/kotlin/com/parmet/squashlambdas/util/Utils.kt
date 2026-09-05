package com.parmet.squashlambdas.util

import java.time.Instant
import java.time.ZoneId
import java.time.chrono.ChronoLocalDateTime

internal val BOSTON = ZoneId.of("America/New_York")

internal fun Instant.inBoston() =
    this.atZone(BOSTON)

internal fun ChronoLocalDateTime<*>.inBoston() =
    this.atZone(BOSTON)
