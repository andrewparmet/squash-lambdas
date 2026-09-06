package com.parmet.squashlambdas

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent
import com.parmet.squashlambdas.notify.OperatorNotifier
import kotlinx.coroutines.runBlocking

abstract class ScheduledLambdaRequestHandler : RequestHandler<ScheduledEvent, Any> {
    protected abstract val notifier: OperatorNotifier

    protected abstract fun initialize()

    protected abstract suspend fun process(input: ScheduledEvent)

    final override fun handleRequest(input: ScheduledEvent, context: Context) {
        runBlocking {
            RequestContext.handle(input, { notifier.publishFailure(it) }) {
                initialize()
                process(input)
            }
        }
    }
}
