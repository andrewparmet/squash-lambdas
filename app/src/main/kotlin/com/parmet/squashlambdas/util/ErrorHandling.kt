package com.parmet.squashlambdas.util

import com.parmet.squashlambdas.Context
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

internal suspend fun HasNotifier.withErrorHandling(input: Any, action: suspend () -> Unit) {
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
