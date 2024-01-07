package com.parmet.squashlambdas

import mu.KotlinLogging
import java.util.concurrent.ConcurrentSkipListMap

object Context {
    private val logger = KotlinLogging.logger { }

    val context = ConcurrentSkipListMap<String, Any>()

    init {
        addToContext("git sha", GIT_SHA)
    }

    fun addToContext(
        key: String,
        value: Any?,
    ) {
        context[key] = value ?: "Value for key $key was null!"
    }

    fun <T> withInput(
        notifier: (Throwable) -> Unit,
        input: Any,
        action: () -> T,
    ) {
        logger.info { "Starting handling of $input" }
        addToContext("input", input)

        try {
            action()
        } catch (ex: Exception) {
            notifier.invoke(ex)
        } finally {
            context.clear()
        }
    }
}
