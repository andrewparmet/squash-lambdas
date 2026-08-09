package com.parmet.squashlambdas.json

import com.parmet.squashlambdas.activity.Activity
import com.parmet.squashlambdas.activity.Clinic
import com.parmet.squashlambdas.activity.Match
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlinx.serialization.json.Json as KotlinJson

object Json {
    val format =
        KotlinJson {
            classDiscriminator = "type"
            encodeDefaults = true
            explicitNulls = false
            ignoreUnknownKeys = true
            serializersModule =
                SerializersModule {
                    polymorphic(Activity::class) {
                        subclass(Match::class, Match.serializer())
                        subclass(Clinic::class, Clinic.serializer())
                    }
                }
        }

    @OptIn(ExperimentalSerializationApi::class)
    private val prettyFormat =
        KotlinJson(format) {
            prettyPrint = true
            prettyPrintIndent = "  "
        }

    inline fun <reified T> encode(value: T): String =
        format.encodeToString(value)

    inline fun <reified T> decode(value: String): T =
        format.decodeFromString(value)

    fun parse(value: String): JsonElement =
        format.parseToJsonElement(value)

    fun prettyPrint(value: JsonElement): String =
        prettyFormat.encodeToString(JsonElement.serializer(), value)
}
