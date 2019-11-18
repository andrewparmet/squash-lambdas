package com.parmet.squashlambdas.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.Instant
import java.time.ZoneId
import java.time.chrono.ChronoLocalDateTime

private val BOSTON = ZoneId.of("America/New_York")

internal fun Instant.inBoston() = this.atZone(BOSTON)

internal fun ChronoLocalDateTime<*>.inBoston() = this.atZone(BOSTON)

internal inline fun <reified T> Gson.fromJson(obj: String): T =
    fromJson(obj, object : TypeToken<T>() {}.type)
