package com.parmet.squashlambdas

import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentSkipListMap

object Context {
    private val logger = KotlinLogging.logger { }

    val context = ConcurrentSkipListMap<String, Any>()

    fun addToContext(key: String, value: Any) {
        context[key] = value
    }

    fun <T> withInput(notifier: (Throwable) -> Unit, input: Any, action: () -> T) {
        addToContext("git sha", GIT_SHA)
        addToContext("input", input)

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
