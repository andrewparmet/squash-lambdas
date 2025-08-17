package com.parmet.squashlambdas.notify

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.parmet.squashlambdas.activity.Court
import com.parmet.squashlambdas.activity.Sport
import com.parmet.squashlambdas.cal.Action

internal object SportSerializer : JsonSerializer<Sport>() {
    override fun serialize(value: Sport, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(value.toString())
    }
}

internal object CourtSerializer : JsonSerializer<Court>() {
    override fun serialize(value: Court, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString("${value.pretty} (${value.sport})")
    }
}

internal object ActionSerializer : JsonSerializer<Action>() {
    override fun serialize(value: Action, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(value.toString())
    }
}
