package com.parmet.squashlambdas.notify

import com.parmet.squashlambdas.activity.Activity
import com.parmet.squashlambdas.activity.Court
import com.parmet.squashlambdas.activity.Sport
import com.parmet.squashlambdas.activity.valueOf
import com.parmet.squashlambdas.cal.Action
import com.parmet.squashlambdas.cal.ChangeSummary
import com.parmet.squashlambdas.clublocker.ReservationResp
import com.parmet.squashlambdas.clublocker.Slot
import com.parmet.squashlambdas.clublocker.StoredToken
import com.parmet.squashlambdas.email.EmailData
import com.parmet.squashlambdas.json.Json
import com.parmet.squashlambdas.reserve.ReservationMaker
import com.parmet.squashlambdas.s3.S3CreateObjectInfo
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

internal object SportSerializer : KSerializer<Sport> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Sport", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Sport) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Sport =
        when (val value = decoder.decodeString()) {
            Sport.Squash.toString() -> Sport.Squash
            Sport.Hardball.toString() -> Sport.Hardball
            Sport.Tennis.toString() -> Sport.Tennis
            Sport.Racquets.toString() -> Sport.Racquets
            Sport.Fitness.toString() -> Sport.Fitness
            else -> error("Unknown sport: $value")
        }
}

internal object CourtSerializer : KSerializer<Court> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Court", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Court) {
        encoder.encodeString("${value.pretty} (${value.sport})")
    }

    override fun deserialize(decoder: Decoder): Court =
        Court.valueOf(decoder.decodeString().substringBefore(" ("))
}

internal object ActionSerializer : KSerializer<Action> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Action", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Action) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Action =
        when (val value = decoder.decodeString()) {
            Action.Create.toString() -> Action.Create
            Action.Update.toString() -> Action.Update
            Action.Delete.toString() -> Action.Delete
            Action.None.toString() -> Action.None
            else -> error("Unknown action: $value")
        }
}

internal fun Any?.toJsonElement(): JsonElement =
    when (this) {
        null -> JsonNull

        is JsonElement -> this

        is String -> JsonPrimitive(this)

        is Boolean -> JsonPrimitive(this)

        is Number -> JsonPrimitive(this)

        is Instant -> JsonPrimitive(toString())

        is LocalDate -> JsonPrimitive(toString())

        is LocalTime -> JsonPrimitive(toString())

        is ChangeSummary -> Json.format.encodeToJsonElement(ChangeSummary.serializer(), this)

        is Activity -> Json.format.encodeToJsonElement(Activity.serializer(), this)

        is Sport -> Json.format.encodeToJsonElement(SportSerializer, this)

        is Court -> Json.format.encodeToJsonElement(CourtSerializer, this)

        is Action -> Json.format.encodeToJsonElement(ActionSerializer, this)

        is EmailData -> Json.format.encodeToJsonElement(EmailData.serializer(), this)

        is Slot -> Json.format.encodeToJsonElement(Slot.serializer(), this)

        is StoredToken -> Json.format.encodeToJsonElement(StoredToken.serializer(), this)

        is S3CreateObjectInfo -> Json.format.encodeToJsonElement(S3CreateObjectInfo.serializer(), this)

        is ReservationMaker.Result.Success ->
            JsonObject(
                mapOf(
                    "match" to match.toJsonElement(),
                    "failures" to failures.toJsonElement()
                )
            )

        is ReservationMaker.Result.Failure ->
            JsonObject(
                mapOf(
                    "date" to date.toJsonElement(),
                    "failures" to failures.toJsonElement()
                )
            )

        is ReservationResp.Error ->
            JsonObject(
                mapOf(
                    "statusCode" to statusCode.toJsonElement(),
                    "message" to message.toJsonElement(),
                    "match" to match.toJsonElement()
                )
            )

        is ReservationResp.Failure ->
            JsonObject(
                mapOf(
                    "t" to t.toJsonElement(),
                    "match" to match.toJsonElement()
                )
            )

        is Throwable -> JsonPrimitive(stackTraceToString())

        is Map<*, *> -> JsonObject(entries.associate { it.key.toString() to it.value.toJsonElement() })

        is Iterable<*> -> JsonArray(map { it.toJsonElement() })

        is Array<*> -> JsonArray(map { it.toJsonElement() })

        else -> JsonPrimitive(toString())
    }
