package com.parmet.squashlambdas

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.S3Event
import com.google.api.services.calendar.Calendar
import com.google.inject.Guice
import com.google.inject.Inject
import com.google.inject.Module
import com.google.inject.name.Named
import com.parmet.squashlambdas.Context.addToContext
import com.parmet.squashlambdas.cal.ChangeSummary
import com.parmet.squashlambdas.cal.EventManagerImpl
import com.parmet.squashlambdas.email.EmailRetriever
import com.parmet.squashlambdas.notify.Notifier
import com.parmet.squashlambdas.s3.S3CreateObjectInfo
import com.parmet.squashlambdas.s3.S3EmailNotification
import io.github.oshai.kotlinlogging.KotlinLogging
import software.amazon.awssdk.services.s3.S3Client

private val logger = KotlinLogging.logger { }

open class EmailNotificationHandler : RequestHandler<S3Event, Any> {
    @Inject
    private lateinit var config: EmailNotificationConfig

    @Inject
    private lateinit var s3: S3Client

    @Inject
    private lateinit var calendar: Calendar

    @Inject
    @Named("myNotifier")
    private lateinit var notifier: Notifier

    private lateinit var retriever: EmailRetriever
    private lateinit var myLambdaUser: LambdaUser

    open val modules: List<Module> = listOf(EmailNotificationModule(), AwsModule())

    fun init() {
        val injector = Guice.createInjector(modules)
        injector.injectMembers(this)

        retriever = EmailRetriever(s3)
        val eventManager = EventManagerImpl(calendar, config.googleCal.calendarId)
        myLambdaUser = SingleLambdaUser(notifier, eventManager)
    }

    final override fun handleRequest(input: S3Event, ignore: Context) {
        init()
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
