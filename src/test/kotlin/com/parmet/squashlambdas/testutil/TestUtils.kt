package com.parmet.squashlambdas.testutil

import com.google.gson.JsonParser
import java.nio.charset.StandardCharsets.UTF_8

val PARSER = JsonParser()

fun removeJsonWhitespace(string: String) = PARSER.parse(string).toString()

fun getJsonResourceAsString(resourceName: String) =
    removeJsonWhitespace(getResourceAsString(getCallerCallerClass(), resourceName))

fun getResourceAsString(resourceName: String) =
    getResourceAsString(getCallerCallerClass(), resourceName)

fun getResourceAsString(owner: Class<*>, resourceName: String) =
    owner.getResourceAsStream(resourceName).reader(UTF_8).use { it.readText() }

private fun getCallerCallerClass() =
    Class.forName(Thread.currentThread().stackTrace[3].className)
