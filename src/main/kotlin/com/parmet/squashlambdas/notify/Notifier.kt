package com.parmet.squashlambdas.notify

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
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.PublishRequest

class Notifier(
    private val sns: SnsClient,
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
            |${print(summary)}
            |
            |Context:
            |${print(context)}
        """.trimMargin()

    fun publishFailedParse(t: Throwable) {
        sns.publish(
            PublishRequest.builder()
                .topicArn(topicArn)
                .message(failedParseMsg(t))
                .subject("Failed to Process Club Locker Email")
                .build()
        )
    }

    private fun failedParseMsg(t: Throwable): String =
        """
            |Encountered an error processing a Club Locker email:
            |
            |Context:
            |${print(context)}
            |
            |Stack trace:
            |${print(t)}
        """.trimMargin()

    fun publishSuccessfulReservation(result: ReservationMaker.Result.Success) {
        sns.publish(
            PublishRequest.builder()
                .topicArn(topicArn)
                .message(successfulParseMsg(result))
                .subject("Made a Reservation on Club Locker")
                .build()
        )
    }

    private fun successfulParseMsg(result: ReservationMaker.Result.Success): String =
        """
            |Successfully made a reservation:
            |${print(result)}
            |
            |Context:
            |${print(context)}
        """.trimMargin()

    fun publishFailedReservation(t: Throwable) {
        sns.publish(
            PublishRequest.builder()
                .topicArn(topicArn)
                .message(failedReservationMsg(t))
                .subject("Failed to make reservation on Club Locker")
                .build()
        )
    }

    private fun failedReservationMsg(t: Throwable): String =
        """
            |Encountered an error making a reservation:
            |
            |Context:
            |${print(context)}
            |
            |Stack trace:
            |${print(t)}
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

    fun publishFailedSlotMonitoring(failure: Throwable) {
        sns.publish(
            PublishRequest.builder()
                .topicArn(topicArn)
                .message(failedSlotMonitoringMsg(failure))
                .subject("Could not track open slots on Club Locker")
                .build()
        )
    }

    private fun failedSlotMonitoringMsg(failure: Throwable): String =
        """
            |Could not monitor slots.
            |
            |Context:
            |${print(context)}
            |
            |Stack trace:
            |${print(failure)}
        """.trimMargin()

    fun publishFailedMatchFind(t: Throwable) {
        sns.publish(
            PublishRequest.builder()
                .topicArn(topicArn)
                .message(failedMatchFindMsg(t))
                .subject("Failed to Process Club Locker Email")
                .build()
        )
    }

    private fun failedMatchFindMsg(t: Throwable): String =
        """
            |Encountered an error processing a MatchFind request:
            |
            |Context:
            |${print(context)}
            |
            |Stack trace:
            |${print(t)}
        """.trimMargin()
}
