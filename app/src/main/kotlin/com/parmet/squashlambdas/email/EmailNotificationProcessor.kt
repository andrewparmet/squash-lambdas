package com.parmet.squashlambdas.email

import com.parmet.squashlambdas.RequestContext.addToContext
import com.parmet.squashlambdas.cal.ChangeSummary
import com.parmet.squashlambdas.cal.ChangeSummaryResolver
import com.parmet.squashlambdas.cal.EventManager
import com.parmet.squashlambdas.clublocker.TokenUpdateHandler
import com.parmet.squashlambdas.json.Json
import com.parmet.squashlambdas.notify.OperatorNotifier
import dev.zacsweers.metro.Inject
@Inject
class EmailNotificationProcessor(
    private val eventManager: EventManager,
    private val changeSummaryResolver: ChangeSummaryResolver,
    private val tokenUpdateHandler: TokenUpdateHandler,
    val notifier: OperatorNotifier
) {
    suspend fun processTokenUpdate(email: EmailData, senderAuthenticated: Boolean): Boolean {
        if (tokenUpdateHandler.isTokenUpdateEmail(email, senderAuthenticated)) {
            tokenUpdateHandler.handle(email)
            return true
        }
        if (tokenUpdateHandler.isTokenUpdateCandidate(email)) {
            throw SecurityException("Rejected unauthenticated token update")
        }
        return false
    }

    suspend fun processCalendar(email: EmailData) {
        ChangeSummary.fromEmail(email)?.let { changeSummaryResolver.resolve(it) }?.also {
            addToContext("changeSummary", Json.element(it))
            it.process(eventManager)
            notifier.publishSuccessfulParse(it)
        }
    }
}
