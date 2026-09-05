package com.parmet.squashlambdas.util

import com.parmet.squashlambdas.Context
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

internal suspend fun withErrorHandling(
    input: Any,
    publishFailure: suspend (Throwable) -> Unit,
    action: suspend () -> Unit
) {
    Context.withInput(
        {
            try {
                publishFailure(it)
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
