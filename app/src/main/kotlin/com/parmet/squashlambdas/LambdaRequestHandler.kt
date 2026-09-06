package com.parmet.squashlambdas

import com.parmet.squashlambdas.notify.OperatorNotifier
import kotlinx.coroutines.runBlocking

abstract class LambdaRequestHandler {
    protected abstract val notifier: OperatorNotifier

    protected abstract fun initialize()

    protected fun handle(input: Any, process: suspend () -> Unit) {
        runBlocking {
            RequestContext.handle(input, { notifier.publishFailure(it) }) {
                initialize()
                process()
            }
        }
    }
}
