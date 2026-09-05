package com.parmet.squashlambdas.notify

import com.google.common.base.CaseFormat
import com.parmet.squashlambdas.aws.TopicPublisher
import com.parmet.squashlambdas.cal.ChangeSummary
import com.parmet.squashlambdas.clublocker.COURTS_BY_ID
import com.parmet.squashlambdas.clublocker.Slot
import com.parmet.squashlambdas.json.Json
import com.parmet.squashlambdas.monitor.TimeFormatter
import com.parmet.squashlambdas.reserve.ReservationMaker
import com.parmet.squashlambdas.util.inBoston
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import java.time.Instant

class OperatorNotifier(
    private val topicPublisher: TopicPublisher,
    private val topicArn: String,
    private val context: Map<String, JsonElement>
) {
    suspend fun publishSuccessfulParse(summary: ChangeSummary) =
        publish(
            "Processed: ${summary.summary()}",
            """
                |Successfully processed change:
                |${print(Json.element(summary))}
                |
                |Context:
                |${print(JsonObject(context))}
            """.trimMargin()
        )

    suspend fun publishSuccessfulReservation(result: ReservationMaker.Result.Success) =
        publish(
            "Made a Reservation on Club Locker",
            """
                |Successfully made a reservation:
                |${print(result.toJsonElement())}
                |
                |Context:
                |${print(JsonObject(context))}
            """.trimMargin()
        )

    suspend fun publishFailure(failure: Throwable) =
        publish(
            "Failed to Execute Club Locker Lambda",
            """
                |Could not execute lambda.
                |
                |Context:
                |${print(JsonObject(context))}
                |
                |Stack trace:
                |${print(failure)}
            """.trimMargin()
        )

    suspend fun publishTokenUpdated(updateTime: Instant) =
        publish("ClubLocker token updated", "ClubLocker token updated successfully at $updateTime")

    suspend fun publishTokenInvalidated(reason: String) =
        publish(
            "ClubLocker token invalid - action required",
            "ClubLocker token has been marked invalid. Reason: $reason. Please send a new token."
        )

    private suspend fun publish(subject: String, message: String) =
        topicPublisher.publish(topicArn, subject, message)
}

class OpenSlotNotifier(
    private val topicPublisher: TopicPublisher,
    private val topicArn: String
) {
    suspend fun publishFoundOpenSlot(result: List<Slot>) =
        topicPublisher.publish(
            topicArn,
            "Squash Monitoring (${Instant.now().inBoston().toLocalDate()}): Found new open slots on Club Locker",
            result.joinToString("\n") { prettyPrint(it) }
        )

    private fun prettyPrint(slot: Slot) =
        "${formatDate(slot)}: ${COURTS_BY_ID.getValue(slot.court).pretty}, " +
            "${TimeFormatter.formatTime(slot.startTime)}-${TimeFormatter.formatTime(slot.endTime)}"

    private fun formatDate(slot: Slot) =
        Instant.ofEpochSecond(slot.startUtc).inBoston().let {
            "${properNoun(it.dayOfWeek.name)}, ${properNoun(it.month.name)} ${it.dayOfMonth}"
        }
}

private fun print(value: JsonElement) =
    try {
        Json.prettyPrint(value)
    } catch (ex: Exception) {
        value.toString() + "[error while formatting JSON: $ex]"
    }.replace("\n", "\n|")

private fun print(throwable: Throwable) =
    throwable.stackTraceToString().replace("\n", "\n|")

private fun properNoun(name: String) =
    CaseFormat.UPPER_UNDERSCORE.converterTo(CaseFormat.UPPER_CAMEL).convert(name)
