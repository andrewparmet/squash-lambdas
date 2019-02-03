package com.parmet.squashlambdas

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.parmet.squashlambdas.cal.ChangeSummary
import com.parmet.squashlambdas.cal.EventManager
import com.parmet.squashlambdas.email.EmailRetriever
import com.parmet.squashlambdas.notify.Notifier
import com.parmet.squashlambdas.s3.S3CreateObjectInfo
import com.parmet.squashlambdas.s3.S3EmailNotification
import mu.KotlinLogging
import java.util.concurrent.ConcurrentSkipListMap

class EmailNotificationHandler : RequestHandler<Any, Any> {
    private val logger = KotlinLogging.logger { }

    private val retriever: EmailRetriever
    private val notifier: Notifier
    private val eventManager: EventManager

    init {
        val config = loadConfiguration(System.getenv("CONFIG_NAME") + ".xml")
        val s3 = configureS3()
        retriever = EmailRetriever(s3)
        notifier = Notifier(configureSns(), config.getString("aws.handledTopicArn"))
        eventManager = EventManager(configureCalendar(config, s3), config.getString("google.calendarId"))
    }

    override fun handleRequest(input: Any, ignore: Context): Any {
        logger.info { "Starting handling of $input" }
        addToContext("input", input)
        try {
            val info = getS3Info(input)
            val email = getEmail(info)
            ChangeSummary.fromEmail(email)?.also {
                addToContext("changeSummary", it)
                it.process(eventManager)
                notifier.publishSuccessfulParse(it)
            }
            return input
        } catch (t: Throwable) {
            logger.error(t) { "Error in email processing" }
            notifier.publishFailedParse(t, context)
            throw t
        }
    }

    private fun getS3Info(input: Any) =
        S3EmailNotification.fromInputObject(input).s3ObjectInfo.also {
            addToContext("s3CreateObjectInfo", it)
        }

    private fun getEmail(info: S3CreateObjectInfo) =
        retriever.retrieveEmail(info.bucketName, info.objectKey).also {
            addToContext("emailData", it)
        }

    companion object {
        private val context = ConcurrentSkipListMap<String, Any>()

        fun addToContext(key: String, value: Any?) {
            context[key] = value ?: "Value for key $key was null!"
        }
    }
}
