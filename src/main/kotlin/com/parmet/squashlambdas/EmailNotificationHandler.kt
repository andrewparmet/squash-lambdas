package com.parmet.squashlambdas

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.S3Event
import com.google.inject.Guice
import com.google.inject.Inject
import com.google.inject.Module
import com.google.inject.name.Named
import com.parmet.squashlambdas.Context.addToContext
import com.parmet.squashlambdas.cal.ChangeSummary
import com.parmet.squashlambdas.cal.EventManager
import com.parmet.squashlambdas.email.EmailRetriever
import com.parmet.squashlambdas.notify.Notifier
import com.parmet.squashlambdas.s3.S3CreateObjectInfo
import com.parmet.squashlambdas.s3.S3EmailNotification
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

open class EmailNotificationHandler : RequestHandler<S3Event, Any> {
    @Inject
    private lateinit var config: EmailNotificationConfig

    @Inject
    @Named("myNotifier")
    private lateinit var notifier: Notifier

    @Inject
    private lateinit var retriever: EmailRetriever

    @Inject
    private lateinit var eventManager: EventManager

    open val modules: List<Module> = listOf(EmailNotificationModule(), AwsModule())

    final override fun handleRequest(input: S3Event, ignore: Context) {
        Guice.createInjector(modules).injectMembers(this)
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
