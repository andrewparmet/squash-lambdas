package com.parmet.squashlambdas

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import java.util.concurrent.ConcurrentSkipListMap

object Context {
    private val logger = KotlinLogging.logger { }

    val context = ConcurrentSkipListMap<String, JsonElement>()

    fun addToContext(key: String, value: JsonElement) {
        context[key] = value
    }

    suspend fun <T> withInput(notifier: suspend (Throwable) -> Unit, input: Any, action: suspend () -> T) {
        addToContext("git sha", JsonPrimitive(GIT_SHA))
        addToContext("input", JsonPrimitive(input.toString()))

        try {
            logger.info { "Starting handling of $input" }
            action()
        } catch (ex: Exception) {
            notifier.invoke(ex)
        } finally {
            context.clear()
        }
    }
}
