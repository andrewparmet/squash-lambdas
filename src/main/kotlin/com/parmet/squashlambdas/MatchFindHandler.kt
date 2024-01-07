package com.parmet.squashlambdas

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.parmet.squashlambdas.Context.addToContext
import com.parmet.squashlambdas.Context.withInput
import com.parmet.squashlambdas.email.EmailRetriever
import com.parmet.squashlambdas.matchfind.CsvEmailSender
import com.parmet.squashlambdas.matchfind.CsvType
import com.parmet.squashlambdas.notify.Notifier
import com.parmet.squashlambdas.s3.S3CreateObjectInfo
import com.parmet.squashlambdas.s3.S3EmailNotification
import mu.KotlinLogging
import org.apache.commons.configuration2.Configuration

class MatchFindHandler : RequestHandler<Any, Any> {
    private val logger = KotlinLogging.logger { }

    private val config: Configuration
    private val notifier: Notifier
    private val retriever: EmailRetriever
    private val sender: CsvEmailSender

    init {
        config = loadConfiguration(System.getenv("CONFIG_NAME") + ".xml")
        notifier = configureNotifier(config.getString("aws.sns.myTopicArn"))
        retriever = EmailRetriever(configureS3())
        sender = CsvEmailSender(configureSes())
    }

    override fun handleRequest(
        input: Any,
        ignore: Context,
    ) = withInput(notifier::publishFailedMatchFind, input) {
        val info = getS3Info(input)
        val email = getEmail(info)

        requireNotNull(email.csvAttachment) {
            "No CSV attachment in MatchFind email"
        }.let {
            val squashCsv = CsvType.SQUASH.filterCsv(it)
            val tennisCsv = CsvType.TENNIS.filterCsv(it)

            config.getString("matchfind.recipient").split(',').map { addr ->
                logger.info { "Sending to $addr" }
                sender.send(squashCsv, tennisCsv, addr)
            }
        }
    }

    private fun getS3Info(input: Any) =
        S3EmailNotification.fromInputObject(input).s3ObjectInfo.also {
            addToContext("s3CreateObjectInfo", it)
        }

    private fun getEmail(info: S3CreateObjectInfo) = retriever.retrieveEmail(info.bucketName, info.objectKey)
}
