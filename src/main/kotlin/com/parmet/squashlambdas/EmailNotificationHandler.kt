package com.parmet.squashlambdas

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.S3Event
import com.parmet.squashlambdas.Context.addToContext
import com.parmet.squashlambdas.cal.ChangeSummary
import com.parmet.squashlambdas.cal.EventManager
import com.parmet.squashlambdas.dagger.DaggerEmailNotificationComponent
import com.parmet.squashlambdas.dagger.EmailNotificationComponent
import com.parmet.squashlambdas.email.EmailRetriever
import com.parmet.squashlambdas.notify.Notifier
import com.parmet.squashlambdas.s3.S3CreateObjectInfo
import com.parmet.squashlambdas.s3.S3EmailNotification
import io.github.oshai.kotlinlogging.KotlinLogging
import javax.inject.Inject
import javax.inject.Named

private val fileLogger = KotlinLogging.logger { }

open class EmailNotificationHandler : AbstractRequestHandler<S3Event>() {
    override val logger = fileLogger

    @Inject
    lateinit var config: EmailNotificationConfig

    @Inject
    @Named("myNotifier")
    lateinit var notifier: Notifier

    @Inject
    lateinit var retriever: EmailRetriever

    @Inject
    lateinit var eventManager: EventManager

    open fun buildComponent(): EmailNotificationComponent =
        DaggerEmailNotificationComponent
            .builder()
            .configName("production-email-notification-handler.yml")
            .build()

    override fun publishFailure(t: Throwable) =
        if (::notifier.isInitialized) {
            notifier.publishFailedParse(t)
        } else {
            null
        }

    final override fun doHandleRequest(input: S3Event, context: Context) {
        buildComponent().inject(this)
        val myLambdaUser = SingleLambdaUser(notifier, eventManager)
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
