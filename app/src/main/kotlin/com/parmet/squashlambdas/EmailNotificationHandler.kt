package com.parmet.squashlambdas

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import com.parmet.squashlambdas.aws.ObjectStorage
import com.parmet.squashlambdas.aws.S3ObjectStorage
import com.parmet.squashlambdas.di.EmailNotificationGraph
import com.parmet.squashlambdas.di.EmailNotificationProcessorProvider
import com.parmet.squashlambdas.email.EmailNotificationProcessor
import com.parmet.squashlambdas.email.EmailRetriever
import com.parmet.squashlambdas.email.SesEmailEvent
import com.parmet.squashlambdas.email.SesEmailRecord
import com.parmet.squashlambdas.json.Json
import com.parmet.squashlambdas.util.SnapStartInitializer
import dev.zacsweers.metro.createGraphFactory
import kotlinx.coroutines.runBlocking
import java.io.InputStream
import java.io.OutputStream

open class EmailNotificationHandler : RequestStreamHandler {
    private val state by lazy {
        runBlocking {
            val objectStorage = buildObjectStorage()
            val routing = loadRoutingConfig(objectStorage)
            val processors =
                routing.tenants.mapValues { (_, tenant) ->
                    RoutedProcessor(tenant, buildGraph(routing.applicationConfig(tenant)).processor)
                }
            HandlerState(routing, EmailRetriever(objectStorage), processors).also {
                require(it.processors.isNotEmpty()) { "At least one email tenant must be configured" }
            }
        }
    }
    private val initializer = SnapStartInitializer { state }

    protected open fun buildObjectStorage(): ObjectStorage =
        S3ObjectStorage()

    protected open suspend fun loadRoutingConfig(objectStorage: ObjectStorage): EmailRoutingConfig {
        val bucket = requireNotNull(System.getenv("EMAIL_CONFIG_BUCKET"))
        val key = requireNotNull(System.getenv("EMAIL_CONFIG_KEY"))
        return Json.decode(objectStorage.read(bucket, key).decodeToString())
    }

    protected open fun buildGraph(config: EmailNotificationConfig): EmailNotificationProcessorProvider =
        createGraphFactory<EmailNotificationGraph.Factory>().create(config)

    final override fun handleRequest(input: InputStream, output: OutputStream, context: Context) {
        initializer.initialize()
        val event = Json.decode<SesEmailEvent>(input.readBytes().decodeToString())
        runBlocking {
            event.records.forEach { record ->
                RequestContext.handle("SES message ${record.ses.mail.messageId}", {
                    state.processors.values.first().processor.notifier.publishFailure(it)
                }) {
                    state.process(record)
                }
            }
        }
    }
}

private data class HandlerState(
    val routing: EmailRoutingConfig,
    val retriever: EmailRetriever,
    val processors: Map<String, RoutedProcessor>
) {
    suspend fun process(record: SesEmailRecord) {
        require(record.ses.receipt.isSafe) { "Rejected unsafe email" }
        require(routing.inboundRecipient.lowercase() in record.ses.receipt.recipients.map(String::lowercase)) {
            "Rejected email for an unexpected SES recipient"
        }
        val objectKey =
            "${routing.inboundEmailPrefix.trimEnd('/')}/${record.ses.mail.messageId}".trimStart('/')
        val email = retriever.retrieveEmail(routing.bucket, objectKey)
        val sharedProcessor = processors.values.first().processor
        if (sharedProcessor.processTokenUpdate(email, record.ses.receipt.dmarcVerdict.passed)) {
            return
        }
        if (!routing.calendarExpectedSender.equals(email.sender, ignoreCase = true) ||
            !record.ses.receipt.dmarcVerdict.passed
        ) {
            throw SecurityException("Rejected unauthenticated calendar email")
        }
        val routed = processors.values.single { it.tenant.matches(email.recipients) }
        routed.processor.processCalendar(email)
    }
}

private data class RoutedProcessor(
    val tenant: EmailTenantConfig,
    val processor: EmailNotificationProcessor
)

private fun EmailTenantConfig.matches(recipients: List<String>): Boolean {
    val normalizedRecipients = recipients.map(String::lowercase).toSet()
    return forwardedRecipient.lowercase() in normalizedRecipients
}
