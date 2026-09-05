package com.parmet.squashlambdas

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import java.util.concurrent.ConcurrentSkipListMap

object RequestContext {
    private val logger = KotlinLogging.logger { }

    val context = ConcurrentSkipListMap<String, JsonElement>()

    fun addToContext(key: String, value: JsonElement) {
        context[key] = value
    }

    suspend fun handle(input: Any, publishFailure: suspend (Throwable) -> Unit, action: suspend () -> Unit) {
        addToContext("git sha", JsonPrimitive(GIT_SHA))
        addToContext("input", JsonPrimitive(input.toString()))

        try {
            logger.info { "Starting handling of $input" }
            action()
        } catch (ex: Exception) {
            try {
                publishFailure(ex)
            } catch (publishFailure: Exception) {
                ex.addSuppressed(publishFailure)
            }
            logger.error(ex) { "Error while handling request" }
        } finally {
            context.clear()
        }
    }
}
