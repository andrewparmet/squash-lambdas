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
import com.parmet.squashlambdas.monitor.TimeFormatter
import com.parmet.squashlambdas.reserve.ReservationMaker
import com.parmet.squashlambdas.util.inBoston
import java.time.Instant

class Notifier(
    private val sns: AmazonSNS,
    private val topicArn: String,
    private val context: Map<*, *>
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

    private fun print(any: Any) =
        printer.toJson(any).replace("\n", "\n|")

    private fun print(t: Throwable) =
        Throwables.getStackTraceAsString(t).replace("\n", "\n|")

    fun publishSuccessfulParse(summary: ChangeSummary) {
        sns.publish(
            PublishRequest(
                topicArn,
                successfulParseMsg(summary),
                "Processed: ${summary.summary()}"
            )
        )
    }

    private fun successfulParseMsg(summary: ChangeSummary): String {
        return """
            |Successfully processed change:
            |${print(summary)}
            |
            |Context:
            |${print(context)}
        """.trimMargin()
    }

    fun publishFailedParse(t: Throwable) {
        sns.publish(
            PublishRequest(
                topicArn,
                failedParseMsg(t),
                "Failed to Process Club Locker Email"
            )
        )
    }

    private fun failedParseMsg(t: Throwable): String {
        return """
            |Encountered an error processing a Club Locker email:
            |
            |Context:
            |${print(context)}
            |
            |Stack trace:
            |${print(t)}
        """.trimMargin()
    }

    fun publishSuccessfulReservation(result: ReservationMaker.Result.Success) {
        sns.publish(
            PublishRequest(
                topicArn,
                successfulParseMsg(result),
                "Made a Reservation on Club Locker"
            )
        )
    }

    private fun successfulParseMsg(result: ReservationMaker.Result.Success): String {
        return """
            |Successfully made a reservation:
            |${print(result)}
            |
            |Context:
            |${print(context)}
        """.trimMargin()
    }

    fun publishFailedReservation(t: Throwable) {
        sns.publish(
            PublishRequest(
                topicArn,
                failedReservationMsg(t),
                "Failed to make reservation on Club Locker"
            )
        )
    }

    private fun failedReservationMsg(t: Throwable): String {
        return """
            |Encountered an error making a reservation:
            |
            |Context:
            |${print(context)}
            |
            |Stack trace:
            |${print(t)}
        """.trimMargin()
    }

    fun publishFoundOpenSlot(result: List<Slot>) {
        sns.publish(
            PublishRequest(
                topicArn,
                foundOpenSlotMsg(result),
                "Squash Monitoring (${Instant.now().inBoston().toLocalDate()}): Found new open slots on Club Locker"
            )
        )
    }

    private fun foundOpenSlotMsg(result: List<Slot>): String {
        return """
            |Found open slots:
            |${result.joinToString("\n") { prettyPrint(it) }}
        """.trimMargin()
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

    fun publishFailedSlotMonitoring(failure: Throwable) {
        sns.publish(
            PublishRequest(
                topicArn,
                failedSlotMonitoringMsg(failure),
                "Could not track open slots on Club Locker"
            )
        )
    }

    private fun failedSlotMonitoringMsg(failure: Throwable): String {
        return """
            |Could not monitor slots.
            |
            |Context:
            |${print(context)}
            |
            |Stack trace:
            |${print(failure)}
        """.trimMargin()
    }

    fun publishFailedMatchFind(t: Throwable) {
        sns.publish(
            PublishRequest(
                topicArn,
                failedMatchFindMsg(t),
                "Failed to Process Club Locker Email"
            )
        )
    }

    private fun failedMatchFindMsg(t: Throwable): String {
        return """
            |Encountered an error processing a MatchFind request:
            |
            |Context:
            |${print(context)}
            |
            |Stack trace:
            |${print(t)}
        """.trimMargin()
    }
}
