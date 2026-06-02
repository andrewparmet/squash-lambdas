package com.parmet.squashlambdas

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.S3Event
import com.parmet.squashlambdas.Context.addToContext
import com.parmet.squashlambdas.cal.ChangeSummary
import com.parmet.squashlambdas.cal.EventManager
import com.parmet.squashlambdas.clublocker.TokenUpdateHandler
import com.parmet.squashlambdas.di.EmailNotificationGraph
import com.parmet.squashlambdas.di.EmailNotificationInjector
import com.parmet.squashlambdas.email.EmailRetriever
import com.parmet.squashlambdas.notify.Notifier
import com.parmet.squashlambdas.s3.S3CreateObjectInfo
import com.parmet.squashlambdas.s3.S3EmailNotification
import com.parmet.squashlambdas.util.HasNotifier
import com.parmet.squashlambdas.util.withErrorHandling
import dev.zacsweers.metro.HasMemberInjections
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.createGraphFactory
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

@HasMemberInjections
open class EmailNotificationHandler :
    RequestHandler<S3Event, Any>,
    HasNotifier {

    @Inject
    lateinit var config: EmailNotificationConfig

    @Inject
    @Named("myNotifier")
    override lateinit var notifier: Notifier

    @Inject
    lateinit var retriever: EmailRetriever

    @Inject
    lateinit var eventManager: EventManager

    @Inject
    lateinit var tokenUpdateHandler: TokenUpdateHandler

    private val graph by lazy { buildGraph() }

    protected open fun buildGraph(): EmailNotificationInjector =
        createGraphFactory<EmailNotificationGraph.Factory>()
            .create("production-email-notification-handler.yml")

    final override fun handleRequest(input: S3Event, context: Context) {
        withErrorHandling(input) {
            graph.inject(this)
            val info = getS3Info(input)
            val email = getEmail(info)

            if (tokenUpdateHandler.isTokenUpdateEmail(email)) {
                tokenUpdateHandler.handle(email)
                return@withErrorHandling
            }

            ChangeSummary.fromEmail(email)?.also {
                addToContext("changeSummary", it)
                if (config.parse.primaryRecipient in email.recipients) {
                    it.process(eventManager)
                    notifier.publishSuccessfulParse(it)
                } else {
                    logger.info { "Not notifying for info: $info" }
                }
            }
        }
    }

    private fun getS3Info(input: S3Event) =
        S3EmailNotification.fromInputObject(input).s3ObjectInfo.also {
            addToContext("s3CreateObjectInfo", it)
        }

    private fun getEmail(info: S3CreateObjectInfo) =
        retriever.retrieveEmail(info.bucketName, info.objectKey).also {
            addToContext("emailData", it)
        }
}
