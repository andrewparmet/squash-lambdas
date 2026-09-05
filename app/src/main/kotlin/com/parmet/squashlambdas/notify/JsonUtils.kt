package com.parmet.squashlambdas.notify

import com.parmet.squashlambdas.activity.Activity
import com.parmet.squashlambdas.activity.Court
import com.parmet.squashlambdas.activity.fromPrettyName
import com.parmet.squashlambdas.clublocker.ReservationResp
import com.parmet.squashlambdas.json.Json
import com.parmet.squashlambdas.reserve.ReservationMaker
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

internal object CourtSerializer : KSerializer<Court> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Court", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Court) {
        encoder.encodeString("${value.pretty} (${value.sport})")
    }

    override fun deserialize(decoder: Decoder): Court =
        Court.fromPrettyName(decoder.decodeString().substringBefore(" ("))
}

internal fun ReservationMaker.Result.toJsonElement(): JsonElement =
    when (this) {
        is ReservationMaker.Result.Success ->
            JsonObject(
                mapOf(
                    "match" to Json.element<Activity>(match),
                    "failures" to JsonArray(failures.map { it.toJsonElement() })
                )
            )

        is ReservationMaker.Result.Failure ->
            JsonObject(
                mapOf(
                    "date" to JsonPrimitive(date.toString()),
                    "failures" to JsonArray(failures.map { it.toJsonElement() })
                )
            )
    }

private fun ReservationResp.NonSuccess.toJsonElement(): JsonElement =
    when (this) {
        is ReservationResp.Error ->
            JsonObject(
                mapOf(
                    "statusCode" to JsonPrimitive(statusCode),
                    "message" to (message?.let(::JsonPrimitive) ?: JsonNull),
                    "match" to Json.element<Activity>(match)
                )
            )

        is ReservationResp.Failure ->
            JsonObject(
                mapOf(
                    "t" to JsonPrimitive(t.stackTraceToString()),
                    "match" to Json.element<Activity>(match)
                )
            )
    }
