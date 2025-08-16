package com.parmet.squashlambdas

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.parmet.squashlambdas.Context.addToContext
import com.parmet.squashlambdas.Context.withInput
import com.parmet.squashlambdas.cal.ChangeSummary
import com.parmet.squashlambdas.cal.EventManagerImpl
import com.parmet.squashlambdas.email.EmailRetriever
import com.parmet.squashlambdas.notify.Notifier
import com.parmet.squashlambdas.s3.S3CreateObjectInfo
import com.parmet.squashlambdas.s3.S3EmailNotification
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

class EmailNotificationHandler : RequestHandler<Any, Any> {
    private val config: AppConfig
    private val s3 = configureS3()
    private val retriever: EmailRetriever
    private val myLambdaUser: LambdaUser

    init {
        config = loadConfiguration(System.getenv("CONFIG_NAME") + ".yml")
        retriever = EmailRetriever(s3)

        val calendar = configureCalendar(config, s3)
        val notifier = configureNotifier(config.aws.sns.myTopicArn)
        val eventManager = EventManagerImpl(calendar, config.google.cal.calendarId)
        myLambdaUser =
            SingleLambdaUser(
                notifier,
                eventManager
            )
    }

    override fun handleRequest(input: Any, ignore: Context) =
        myLambdaUser.withInput(Notifier::publishFailedParse, input) {
            val info = getS3Info(input)
            val email = getEmail(info)
            ChangeSummary.fromEmail(email)?.also {
                addToContext("changeSummary", it)
                if (config.parse.primaryRecipient in email.recipients) {
                    myLambdaUser.handleEmail(it)
                } else {
                    logger.info { "Not notifying for info: $info" }
                }
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
}
