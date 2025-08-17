package com.parmet.squashlambdas.util

import com.parmet.squashlambdas.Context
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

fun HasNotifier.withErrorHandling(input: Any, action: () -> Unit) {
    Context.withInput(
        {
            try {
                notifier.publishFailure(it)
            } catch (ex: Exception) {
                it.addSuppressed(ex)
            }
            logger.error(it) { "Error while handling request" }
        },
        input,
        {
            logger.info { "Beginning handling" }
            action()
        }
    )
}
