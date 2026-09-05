package com.parmet.squashlambdas.testutil

import java.nio.charset.StandardCharsets.UTF_8
import kotlin.reflect.KClass

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
