package com.parmet.squashlambdas

import com.parmet.squashlambdas.cal.ChangeSummary
import com.parmet.squashlambdas.cal.EventManager
import com.parmet.squashlambdas.notify.Notifier

interface LambdaUser {
    fun withInput(
        notifier: (Notifier, Throwable) -> Unit,
        input: Any,
        action: () -> Unit
    )

    fun handleEmail(changeSummary: ChangeSummary)
}

class SingleLambdaUser(
    private val notifier: Notifier,
    private val eventManager: EventManager
) : LambdaUser {
    override fun handleEmail(changeSummary: ChangeSummary) {
        changeSummary.process(eventManager)
        notifier.publishSuccessfulParse(changeSummary)
    }

    override fun withInput(notifier: (Notifier, Throwable) -> Unit, input: Any, action: () -> Unit) =
        Context.withInput({ notifier(this.notifier, it) }, input, action)
}

class CompoundLambdaUser(
    private val lambdaUsers: List<SingleLambdaUser>
) : LambdaUser {
    override fun handleEmail(changeSummary: ChangeSummary) {
        lambdaUsers.forEach { it.handleEmail(changeSummary) }
    }

    override fun withInput(notifier: (Notifier, Throwable) -> Unit, input: Any, action: () -> Unit) {
        lambdaUsers.forEach { it.withInput(notifier, input, action) }
    }
}
