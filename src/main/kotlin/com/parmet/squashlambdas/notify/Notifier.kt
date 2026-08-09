package com.parmet.squashlambdas.notify

import com.google.common.base.CaseFormat
import com.parmet.squashlambdas.cal.ChangeSummary
import com.parmet.squashlambdas.clublocker.COURTS_BY_ID
import com.parmet.squashlambdas.clublocker.Slot
import com.parmet.squashlambdas.json.Json
import com.parmet.squashlambdas.monitor.TimeFormatter
import com.parmet.squashlambdas.reserve.ReservationMaker
import com.parmet.squashlambdas.util.inBoston
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.PublishRequest
import java.time.Instant

class Notifier(
    private val sns: SnsClient,
    private val topicArn: String,
    private val context: Map<String, JsonElement>
) {
    private fun print(value: JsonElement) =
        try {
            Json.prettyPrint(value)
        } catch (ex: Exception) {
            value.toString() + "[error while formatting JSON: $ex]"
        }.replace("\n", "\n|")

    private fun print(t: Throwable) =
        t.stackTraceToString().replace("\n", "\n|")

    fun publishSuccessfulParse(summary: ChangeSummary) {
        sns.publish(
            PublishRequest.builder()
                .topicArn(topicArn)
                .message(successfulParseMsg(summary))
                .subject("Processed: ${summary.summary()}")
                .build()
        )
    }

    private fun successfulParseMsg(summary: ChangeSummary): String =
        """
            |Successfully processed change:
            |${print(Json.element(summary))}
            |
            |Context:
            |${print(JsonObject(context))}
        """.trimMargin()

    fun publishSuccessfulReservation(result: ReservationMaker.Result.Success) {
        sns.publish(
            PublishRequest.builder()
                .topicArn(topicArn)
                .message(successfulReservationMsg(result))
                .subject("Made a Reservation on Club Locker")
                .build()
        )
    }

    private fun successfulReservationMsg(result: ReservationMaker.Result.Success): String =
        """
            |Successfully made a reservation:
            |${print(result.toJsonElement())}
            |
            |Context:
            |${print(JsonObject(context))}
        """.trimMargin()

    fun publishFoundOpenSlot(result: List<Slot>) {
        sns.publish(
            PublishRequest.builder()
                .topicArn(topicArn)
                .message(foundOpenSlotMsg(result))
                .subject(
                    "Squash Monitoring (${
                        Instant.now().inBoston().toLocalDate()
                    }): Found new open slots on Club Locker"
                )
                .build()
        )
    }

    private fun foundOpenSlotMsg(result: List<Slot>): String =
        """
            |Found open slots:
            |${result.joinToString("\n") { prettyPrint(it) }}
        """.trimMargin()

    private fun properNoun(name: String) =
        CaseFormat.UPPER_UNDERSCORE.converterTo(CaseFormat.UPPER_CAMEL).convert(name)

    private fun prettyPrint(slot: Slot) =
        "${formatDate(slot)}: ${COURTS_BY_ID.getValue(slot.court).pretty}, " +
            "${TimeFormatter.formatTime(slot.startTime)}-${TimeFormatter.formatTime(slot.endTime)}"

    private fun formatDate(slot: Slot) =
        Instant.ofEpochSecond(slot.startUtc).inBoston().let {
            "${properNoun(it.dayOfWeek.name)}, ${properNoun(it.month.name)} ${it.dayOfMonth}"
        }

    fun publishFailure(t: Throwable) {
        sns.publish(
            PublishRequest.builder()
                .topicArn(topicArn)
                .message(failureMsg(t))
                .subject("Failed to Execute Club Locker Lambda")
                .build()
        )
    }

    private fun failureMsg(failure: Throwable): String =
        """
            |Could not execute lambda.
            |
            |Context:
            |${print(JsonObject(context))}
            |
            |Stack trace:
            |${print(failure)}
        """.trimMargin()

    fun publishTokenUpdated(updateTime: Instant) {
        sns.publish(
            PublishRequest.builder()
                .topicArn(topicArn)
                .message("ClubLocker token updated successfully at $updateTime")
                .subject("ClubLocker token updated")
                .build()
        )
    }

    fun publishTokenInvalidated(reason: String) {
        sns.publish(
            PublishRequest.builder()
                .topicArn(topicArn)
                .message("ClubLocker token has been marked invalid. Reason: $reason. Please send a new token.")
                .subject("ClubLocker token invalid - action required")
                .build()
        )
    }
}
