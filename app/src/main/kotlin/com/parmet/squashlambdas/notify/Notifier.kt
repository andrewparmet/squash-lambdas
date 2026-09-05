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

class Notifier(
    private val topicPublisher: TopicPublisher,
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

    suspend fun publishSuccessfulParse(summary: ChangeSummary) =
        publish("Processed: ${summary.summary()}", successfulParseMsg(summary))

    private fun successfulParseMsg(summary: ChangeSummary): String =
        """
            |Successfully processed change:
            |${print(Json.element(summary))}
            |
            |Context:
            |${print(JsonObject(context))}
        """.trimMargin()

    suspend fun publishSuccessfulReservation(result: ReservationMaker.Result.Success) =
        publish("Made a Reservation on Club Locker", successfulReservationMsg(result))

    private fun successfulReservationMsg(result: ReservationMaker.Result.Success): String =
        """
            |Successfully made a reservation:
            |${print(result.toJsonElement())}
            |
            |Context:
            |${print(JsonObject(context))}
        """.trimMargin()

    suspend fun publishFoundOpenSlot(result: List<Slot>) =
        publish(
            "Squash Monitoring (${Instant.now().inBoston().toLocalDate()}): Found new open slots on Club Locker",
            foundOpenSlotMsg(result),
        )

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

    suspend fun publishFailure(t: Throwable) =
        publish("Failed to Execute Club Locker Lambda", failureMsg(t))

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

    suspend fun publishTokenUpdated(updateTime: Instant) =
        publish("ClubLocker token updated", "ClubLocker token updated successfully at $updateTime")

    suspend fun publishTokenInvalidated(reason: String) =
        publish(
            "ClubLocker token invalid - action required",
            "ClubLocker token has been marked invalid. Reason: $reason. Please send a new token.",
        )

    private suspend fun publish(subject: String, message: String) =
        topicPublisher.publish(topicArn, subject, message)
}
