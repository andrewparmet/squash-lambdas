package com.parmet.squashlambdas.notify

import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.PublishRequest
import com.fatboyindustrial.gsonjavatime.Converters
import com.google.common.base.CaseFormat
import com.google.common.base.Throwables
import com.google.gson.GsonBuilder
import com.parmet.squashlambdas.activity.Court
import com.parmet.squashlambdas.activity.Sport
import com.parmet.squashlambdas.cal.Action
import com.parmet.squashlambdas.cal.ChangeSummary
import com.parmet.squashlambdas.clublocker.COURTS_BY_ID
import com.parmet.squashlambdas.clublocker.Slot
import com.parmet.squashlambdas.inBoston
import com.parmet.squashlambdas.monitor.TimeFormatter
import com.parmet.squashlambdas.reserve.ReservationMaker
import java.time.Instant

internal class Notifier(
    private val sns: AmazonSNS,
    private val myTopicArn: String,
    private val publicTopicArn: String
) {
    private val printer =
        GsonBuilder()
            .registerAllJavaTimeAdapters()
            .registerTypeAdapterFactory(ACTIVITY_ADAPTER_FACTORY)
            .registerTypeHierarchyAdapter(Sport::class.java, SportSerializer)
            .registerTypeHierarchyAdapter(Court::class.java, CourtSerializer)
            .registerTypeHierarchyAdapter(Action::class.java, ActionSerializer)
            .serializeNulls()
            .setPrettyPrinting()
            .create()

    private fun GsonBuilder.registerAllJavaTimeAdapters() =
        Converters.registerAll(this)

    fun publishSuccessfulParse(summary: ChangeSummary) {
        sns.publish(
            PublishRequest(
                myTopicArn,
                successfulParseMsg(summary),
                "Processed Club Locker Email"
            )
        )
    }

    private fun successfulParseMsg(summary: ChangeSummary): String {
        return """
Successfully processed change:
${printer.toJson(summary)}
        """
    }

    fun publishFailedParse(t: Throwable, context: Map<*, *>) {
        sns.publish(
            PublishRequest(
                myTopicArn,
                failedParseMsg(t, context),
                "Failed to Process Club Locker Email"
            )
        )
    }

    private fun failedParseMsg(t: Throwable, context: Map<*, *>): String {
        return """
Encountered an error processing a Club Locker email:

Context:
${printer.toJson(context)}

Stack trace:
${Throwables.getStackTraceAsString(t)}
        """
    }

    fun publishSuccessfulReservation(result: ReservationMaker.Result.Success) {
        sns.publish(
            PublishRequest(
                myTopicArn,
                successfulParseMsg(result),
                "Made a Reservation on Club Locker"
            )
        )
    }

    private fun successfulParseMsg(result: ReservationMaker.Result.Success): String {
        return """
Successfully made a reservation:
${printer.toJson(result)}
        """
    }

    fun publishFailedReservation(t: Throwable, context: Map<*, *>) {
        sns.publish(
            PublishRequest(
                myTopicArn,
                failedReservationMsg(t, context),
                "Failed to Make Reservation on Club Locker"
            )
        )
    }

    private fun failedReservationMsg(t: Throwable, context: Map<*, *>): String {
        return """
Encountered an error making a reservation:

Context:
${printer.toJson(context)}

Stack trace:
${Throwables.getStackTraceAsString(t)}
        """
    }

    fun publishFailedReservation(result: ReservationMaker.Result.Failure, context: Map<*, *>) {
        sns.publish(
            PublishRequest(
                myTopicArn,
                failedReservationMsg(result, context),
                "Failed to Make Reservation on Club Locker"
            )
        )
    }

    private fun failedReservationMsg(result: ReservationMaker.Result.Failure, context: Map<*, *>): String {
        return """
Could not make a reservation:

Context:
${printer.toJson(context)}

Failures:
$result
        """
    }

    fun publishFoundOpenSlot(result: List<Slot>) {
        sns.publish(
            PublishRequest(
                publicTopicArn,
                foundOpenSlotMsg(result),
                "Squash Monitoring (${Instant.now().inBoston().toLocalDate()}): Found new open slots on Club Locker"
            )
        )
    }

    private fun foundOpenSlotMsg(result: List<Slot>): String {
        return """
Found open slots:
${result.joinToString("\n") { prettyPrint(it) }}
        """
    }

    private fun properNoun(name: String) =
        CaseFormat.UPPER_UNDERSCORE.converterTo(CaseFormat.UPPER_CAMEL).convert(name)

    private fun prettyPrint(slot: Slot) =
        "${formatDate(slot)}: ${COURTS_BY_ID.getValue(slot.court).pretty}, " +
            "${TimeFormatter.formatTime(slot.startTime)}-${TimeFormatter.formatTime(slot.endTime)}"

    private fun formatDate(slot: Slot) =
        Instant.ofEpochSecond(slot.startUtc).inBoston().let {
            "${properNoun(it.dayOfWeek.name)}, ${properNoun(it.month.name)} ${it.dayOfMonth}"
        }

    fun publishFailedSlotMonitoring(failure: Throwable, context: Map<*, *>) {
        sns.publish(
            PublishRequest(
                myTopicArn,
                failedSlotMonitoringMsg(failure, context),
                "Could not track open slots on Club Locker"
            )
        )
    }

    private fun failedSlotMonitoringMsg(failure: Throwable, context: Map<*, *>): String {
        return """
Could not monitor slots.

Context:
${printer.toJson(context)}

Stack trace:
${Throwables.getStackTraceAsString(failure)}
        """
    }
}
