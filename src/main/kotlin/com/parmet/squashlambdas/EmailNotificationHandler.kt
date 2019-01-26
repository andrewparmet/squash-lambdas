package com.parmet.squashlambdas

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.sns.AmazonSNS
import com.fatboyindustrial.gsonjavatime.InstantConverter
import com.google.api.services.calendar.Calendar
import com.google.common.base.Throwables
import com.google.gson.GsonBuilder
import com.parmet.squashlambdas.cal.ChangeSummary
import com.parmet.squashlambdas.cal.EventManager
import com.parmet.squashlambdas.email.EmailRetriever
import com.parmet.squashlambdas.s3.S3CreateObjectInfo
import com.parmet.squashlambdas.s3.S3EmailNotification
import mu.KotlinLogging
import java.time.Instant
import java.util.concurrent.ConcurrentSkipListMap

class EmailNotificationHandler : RequestHandler<Any, Any> {
    private val logger = KotlinLogging.logger { }

    private val printer =
        GsonBuilder()
            .registerTypeAdapter(Instant::class.java, InstantConverter())
            .serializeNulls()
            .setPrettyPrinting()
            .create()

    private val s3: AmazonS3
    private val sns: AmazonSNS
    private val calendar: Calendar

    private val calendarId: String
    private val topicArn: String

    init {
        val config = loadConfiguration(System.getenv("CONFIG_NAME") + ".xml")
        s3 = configureS3()
        sns = configureSns()
        calendar = configureCalendar(config, s3)
        calendarId = config.getString("google.calendarId")
        topicArn = config.getString("aws.handledTopicArn")
    }

    override fun handleRequest(input: Any, ignore: Context): Any {
        logger.info { "Starting handling of $input" }
        addToContext("input", input)
        try {
            val info = getS3Info(input)
            val email = getEmail(info)
            ChangeSummary.fromEmail(email)?.also {
                addToContext("changeSummary", it)
                it.process(EventManager(calendar, calendarId))
                publishSuccess(it)
            }
            return input
        } catch (t: Throwable) {
            logger.error(t) { "Error in email processing" }
            publishFailure(t)
            throw t
        }
    }

    private fun getS3Info(input: Any) =
        S3EmailNotification.fromInputObject(input).s3ObjectInfo.also {
            addToContext("s3CreateObjectInfo", it)
        }

    private fun getEmail(info: S3CreateObjectInfo) =
        EmailRetriever(s3, info.bucketName, info.objectKey).retrieveEmail().also {
            addToContext("emailData", it)
        }

    private fun publishSuccess(summary: ChangeSummary) {
        sns.publish(
            topicArn,
            """
Successfully processed change:
  ${printer.toJson(summary)}
            """,
            "Processed Club Locker Email")
    }

    private fun publishFailure(t: Throwable) {
        sns.publish(
            topicArn,
            """
Encountered an error processing a Club Locker email:

Context:
${printer.toJson(context)}

Stack trace:
${Throwables.getStackTraceAsString(t)}
            """,
            "Failed to Process Club Locker Email")
    }

    companion object {
        private val context = ConcurrentSkipListMap<String, Any>()

        fun addToContext(key: String, value: Any) {
            context[key] = value
        }
    }
}
