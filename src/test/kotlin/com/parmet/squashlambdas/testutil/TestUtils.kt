package com.parmet.squashlambdas.testutil

import com.parmet.squashlambdas.json.Json
import java.nio.charset.StandardCharsets.UTF_8
import kotlin.reflect.KClass

fun removeJsonWhitespace(string: String) =
    Json.mapper.readTree(string).toString()

fun getJsonResourceAsString(resourceName: String) =
    removeJsonWhitespace(getResourceAsString(getCallerCallerClass(), resourceName))

fun getResourceAsString(resourceName: String) =
    getResourceAsString(getCallerCallerClass(), resourceName)

fun getResourceAsString(owner: KClass<*>, resourceName: String) =
    getResourceAsString(owner.java, resourceName)

fun getResourceAsString(owner: Class<*>, resourceName: String) =
    owner.getResourceAsStream(resourceName)
        .reader(UTF_8)
        .use { it.readText() }

private fun getCallerCallerClass() =
    Class.forName(Thread.currentThread().stackTrace[3].className)
