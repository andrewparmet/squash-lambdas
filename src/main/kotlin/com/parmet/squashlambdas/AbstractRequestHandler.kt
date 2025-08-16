package com.parmet.squashlambdas

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import io.github.oshai.kotlinlogging.KLogger

abstract class AbstractRequestHandler<T> : RequestHandler<T, Any> {
    abstract val logger: KLogger

    final override fun handleRequest(input: T, context: Context) {
        try {
            logger.info { "Beginning handling" }
            doHandleRequest(input, context)
        } catch (ex: Exception) {
            publishFailure(ex) ?: logger.error(ex) { "Error while monitoring slots" }
        }
    }

    abstract fun doHandleRequest(input: T, context: Context)

    abstract fun publishFailure(t: Throwable): Any?
}
