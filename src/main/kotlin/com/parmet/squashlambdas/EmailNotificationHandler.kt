package com.parmet.squashlambdas

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.parmet.squashlambdas.Context.addToContext
import com.parmet.squashlambdas.activity.Activity
import com.parmet.squashlambdas.cal.ChangeSummary
import com.parmet.squashlambdas.cal.EventManager
import com.parmet.squashlambdas.cal.EventManagerImpl
import com.parmet.squashlambdas.email.EmailRetriever
import com.parmet.squashlambdas.notify.Notifier
import com.parmet.squashlambdas.s3.S3CreateObjectInfo
import com.parmet.squashlambdas.s3.S3EmailNotification

class EmailNotificationHandler : RequestHandler<Any, Any> {
    private val retriever: EmailRetriever
    private val myLambdaUser: LambdaUser
    private val secondaryLambdaUser: LambdaUser

    init {
        val config = loadConfiguration(System.getenv("CONFIG_NAME") + ".xml")
        val s3 = configureS3()
        retriever = EmailRetriever(s3)

        val calendar = configureCalendar(config, s3)
        val myNotifier = configureNotifier(config.getString("aws.sns.myTopicArn"))
        myLambdaUser =
            SingleLambdaUser(
                myNotifier,
                EventManagerImpl(calendar, config.getString("google.cal.calendarId"))
            )

        secondaryLambdaUser =
            CompoundLambdaUser(
                listOf(
                    SingleLambdaUser(
                        myNotifier,
                        object : EventManager {
                            override fun create(activity: Activity) = Unit
                            override fun update(activity: Activity) = Unit
                            override fun delete(activity: Activity) = Unit
                        }
                    ),
                    SingleLambdaUser(
                        configureNotifier(config.getString("aws.sns.secondaryTopicArn")),
                        EventManagerImpl(calendar, config.getString("google.cal.secondaryCalendarId"))
                    )
                )
            )
    }

    override fun handleRequest(input: Any, ignore: Context) =
        myLambdaUser.withInput(Notifier::publishFailedParse, input) {
            val info = getS3Info(input)
            val email = getEmail(info)
            ChangeSummary.fromEmail(email)?.also {
                addToContext("changeSummary", it)
                if (email.recipients.singleOrNull() == "squash@andrew.parmet.com") {
                    myLambdaUser.handleEmail(it)
                } else {
                    secondaryLambdaUser.handleEmail(it)
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
