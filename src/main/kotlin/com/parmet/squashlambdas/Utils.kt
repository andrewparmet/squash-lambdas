package com.parmet.squashlambdas

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.ZoneId

internal val BOSTON = ZoneId.of("America/New_York")

internal inline fun <reified T> Gson.fromJson(obj: String): T =
    fromJson(obj, object : TypeToken<T>() {}.type)
