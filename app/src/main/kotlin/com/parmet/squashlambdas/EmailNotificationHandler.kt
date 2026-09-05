package com.parmet.squashlambdas

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import com.parmet.squashlambdas.aws.S3ObjectStorage
import com.parmet.squashlambdas.di.EmailNotificationGraph
import com.parmet.squashlambdas.di.EmailNotificationProcessorProvider
import com.parmet.squashlambdas.email.EmailNotificationProcessor
import com.parmet.squashlambdas.email.SesEmailEvent
import com.parmet.squashlambdas.json.Json
import com.parmet.squashlambdas.util.SnapStartInitializer
import dev.zacsweers.metro.createGraphFactory
import kotlinx.coroutines.runBlocking
import java.io.InputStream
import java.io.OutputStream

open class EmailNotificationHandler : RequestStreamHandler {
    private val processors by lazy {
        runBlocking {
            val routing = loadRoutingConfig()
            routing.tenants.mapValues { (_, tenant) ->
                RoutedProcessor(tenant, buildGraph(routing.applicationConfig(tenant)).processor)
            }
        }
    }
    private val initializer = SnapStartInitializer { processors }

    protected open suspend fun loadRoutingConfig(): EmailRoutingConfig {
        val bucket = requireNotNull(System.getenv("EMAIL_CONFIG_BUCKET"))
        val key = requireNotNull(System.getenv("EMAIL_CONFIG_KEY"))
        return Json.decode(S3ObjectStorage().read(bucket, key).decodeToString())
    }

    protected open fun buildGraph(config: EmailNotificationConfig): EmailNotificationProcessorProvider =
        createGraphFactory<EmailNotificationGraph.Factory>().create(config)

    final override fun handleRequest(input: InputStream, output: OutputStream, context: Context) {
        initializer.initialize()
        val event = Json.decode<SesEmailEvent>(input.readBytes().decodeToString())
        runBlocking {
            event.records.forEach { record ->
                RequestContext.handle("SES message ${record.ses.mail.messageId}", {
                    processors.values.first().processor.notifier.publishFailure(it)
                }) {
                    val routed =
                        processors.values.single { candidate ->
                            candidate.tenant.matches(record.ses.receipt.recipients)
                        }
                    routed.processor.process(record)
                }
            }
        }
    }
}

private data class RoutedProcessor(
    val tenant: EmailTenantConfig,
    val processor: EmailNotificationProcessor
)

private fun EmailTenantConfig.matches(recipients: List<String>): Boolean {
    val normalizedRecipients = recipients.map(String::lowercase).toSet()
    return inboundRecipients.any { it.lowercase() in normalizedRecipients }
}
