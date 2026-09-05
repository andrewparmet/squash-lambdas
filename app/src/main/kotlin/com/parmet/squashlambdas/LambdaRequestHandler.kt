package com.parmet.squashlambdas

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.parmet.squashlambdas.notify.OperatorNotifier
import kotlinx.coroutines.runBlocking

abstract class LambdaRequestHandler<Input : Any> : RequestHandler<Input, Any> {
    protected abstract val notifier: OperatorNotifier

    protected abstract fun initialize()

    protected abstract suspend fun process(input: Input)

    final override fun handleRequest(input: Input, context: Context) {
        runBlocking {
            RequestContext.handle(input, { notifier.publishFailure(it) }) {
                initialize()
                process(input)
            }
        }
    }
}
