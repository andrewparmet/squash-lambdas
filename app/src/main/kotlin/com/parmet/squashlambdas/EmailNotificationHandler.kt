package com.parmet.squashlambdas

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.S3Event
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord
import com.parmet.squashlambdas.Context.addToContext
import com.parmet.squashlambdas.cal.ChangeSummary
import com.parmet.squashlambdas.cal.ChangeSummaryResolver
import com.parmet.squashlambdas.cal.EventManager
import com.parmet.squashlambdas.clublocker.TokenUpdateHandler
import com.parmet.squashlambdas.di.EmailNotificationGraph
import com.parmet.squashlambdas.di.EmailNotificationInjector
import com.parmet.squashlambdas.email.EmailRetriever
import com.parmet.squashlambdas.json.Json
import com.parmet.squashlambdas.notify.Notifier
import com.parmet.squashlambdas.s3.S3CreateObjectInfo
import com.parmet.squashlambdas.s3.S3EmailNotification
import com.parmet.squashlambdas.util.HasNotifier
import com.parmet.squashlambdas.util.SnapStartInitializer
import com.parmet.squashlambdas.util.withErrorHandling
import dev.zacsweers.metro.HasMemberInjections
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Named
import dev.zacsweers.metro.createGraphFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking

private val logger = KotlinLogging.logger { }
private const val SES_SETUP_NOTIFICATION = "AMAZON_SES_SETUP_NOTIFICATION"

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
    lateinit var changeSummaryResolver: ChangeSummaryResolver

    @Inject
    lateinit var tokenUpdateHandler: TokenUpdateHandler

    private val graph by lazy { buildGraph() }
    private val initializer = SnapStartInitializer { graph.inject(this) }

    protected open fun buildGraph(): EmailNotificationInjector =
        createGraphFactory<EmailNotificationGraph.Factory>()
            .create("production-email-notification-handler.conf")

    final override fun handleRequest(input: S3Event, context: Context) {
        runBlocking {
            input.records.forEach { record ->
                withErrorHandling(record) {
                    process(record)
                }
            }
        }
    }

    private suspend fun process(record: S3EventNotificationRecord) {
        initializer.initialize()
        val info = getS3Info(record)
        if (info.objectKey.substringAfterLast('/') == SES_SETUP_NOTIFICATION) {
            logger.info { "Ignoring the Amazon SES setup notification" }
            return
        }
        val email = getEmail(info)

        if (tokenUpdateHandler.isTokenUpdateEmail(email)) {
            tokenUpdateHandler.handle(email)
            return
        }

        ChangeSummary.fromEmail(email)?.let { changeSummaryResolver.resolve(it) }?.also {
            addToContext("changeSummary", Json.element(it))
            if (config.parse.primaryRecipient in email.recipients) {
                it.process(eventManager)
                notifier.publishSuccessfulParse(it)
            } else {
                logger.info { "Not notifying for info: $info" }
            }
        }
    }

    private fun getS3Info(record: S3EventNotificationRecord) =
        S3EmailNotification.fromRecord(record).s3ObjectInfo.also {
            addToContext("s3CreateObjectInfo", Json.element(it))
        }

    private suspend fun getEmail(info: S3CreateObjectInfo) =
        retriever.retrieveEmail(info.bucketName, info.objectKey).also {
            addToContext("emailData", Json.element(it))
        }
}
