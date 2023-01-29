package com.parmet.squashlambdas

import mu.KotlinLogging
import java.util.concurrent.ConcurrentSkipListMap
import kotlin.reflect.KFunction2

object Context {
    private val logger = KotlinLogging.logger { }
    private val context = ConcurrentSkipListMap<String, Any>()

    init {
        addToContext("git sha", GIT_SHA)
    }

    fun addToContext(key: String, value: Any?) {
        context[key] = value ?: "Value for key $key was null!"
    }

    fun <T> withInput(notifier: KFunction2<Throwable, Map<*, *>, Unit>, input: Any, action: () -> T): T? {
        logger.info { "Starting handling of $input" }
        addToContext("input", input)

        return try {
            action()
        } catch (ex: Exception) {
            notifier.invoke(ex, context)
            null
        } finally {
            context.clear()
        }
    }
}
