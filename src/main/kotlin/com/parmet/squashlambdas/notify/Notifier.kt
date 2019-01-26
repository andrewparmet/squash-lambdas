package com.parmet.squashlambdas.notify

import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.PublishRequest
import com.fatboyindustrial.gsonjavatime.InstantConverter
import com.google.common.base.Throwables
import com.google.gson.GsonBuilder
import com.parmet.squashlambdas.cal.ChangeSummary
import java.time.Instant

class Notifier(
    private val sns: AmazonSNS,
    private val topicArn: String
) {
    private val printer =
        GsonBuilder()
            .registerTypeAdapter(Instant::class.java, InstantConverter())
            .serializeNulls()
            .setPrettyPrinting()
            .create()

    fun publishSuccess(summary: ChangeSummary) {
        sns.publish(
            PublishRequest(
                topicArn,
                successMsg(summary),
                "Processed Club Locker Email"))
    }

    private fun successMsg(summary: ChangeSummary): String {
        return """
Successfully processed change:
${printer.toJson(summary)}
        """
    }

    fun publishFailure(t: Throwable, context: Map<*, *>) {
        sns.publish(
            PublishRequest(
                topicArn,
                failureMsg(t, context),
                "Failed to Process Club Locker Email"))
    }

    private fun failureMsg(t: Throwable, context: Map<*, *>): String {
        return """
Encountered an error processing a Club Locker email:

Context:
${printer.toJson(context)}

Stack trace:
${Throwables.getStackTraceAsString(t)}
        """
    }
}
