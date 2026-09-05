package com.parmet.squashlambdas.email

import com.parmet.squashlambdas.Context.addToContext
import com.parmet.squashlambdas.EmailNotificationConfig
import com.parmet.squashlambdas.cal.ChangeSummary
import com.parmet.squashlambdas.cal.ChangeSummaryResolver
import com.parmet.squashlambdas.cal.EventManager
import com.parmet.squashlambdas.clublocker.TokenUpdateHandler
import com.parmet.squashlambdas.json.Json
import com.parmet.squashlambdas.notify.OperatorNotifier
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

@Inject
class EmailNotificationProcessor(
    private val config: EmailNotificationConfig,
    private val retriever: EmailRetriever,
    private val eventManager: EventManager,
    private val changeSummaryResolver: ChangeSummaryResolver,
    private val tokenUpdateHandler: TokenUpdateHandler,
    val notifier: OperatorNotifier
) {
    suspend fun process(record: SesEmailRecord) {
        require(record.ses.receipt.isSafe) { "Rejected unsafe email" }
        val objectKey = "${config.parse.inboundEmailPrefix.trimEnd('/')}/${record.ses.mail.messageId}".trimStart('/')
        val email = retriever.retrieveEmail(config.parse.inboundEmailBucket, objectKey)

        if (tokenUpdateHandler.isTokenUpdateEmail(email, record.ses.receipt.dmarcVerdict.passed)) {
            tokenUpdateHandler.handle(email)
            return
        }
        if (tokenUpdateHandler.isTokenUpdateCandidate(email)) {
            throw SecurityException("Rejected unauthenticated token update")
        }
        if (!config.parse.expectedSender.equals(email.sender, ignoreCase = true) ||
            !record.ses.receipt.dmarcVerdict.passed
        ) {
            throw SecurityException("Rejected unauthenticated calendar email")
        }
        if (config.parse.primaryRecipient.lowercase() !in email.recipients) {
            logger.info { "Ignoring email without the configured forwarded recipient" }
            return
        }

        ChangeSummary.fromEmail(email)?.let { changeSummaryResolver.resolve(it) }?.also {
            addToContext("changeSummary", Json.element(it))
            it.process(eventManager)
            notifier.publishSuccessfulParse(it)
        }
    }
}
