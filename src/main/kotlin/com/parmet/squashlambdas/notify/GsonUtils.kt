package com.parmet.squashlambdas.notify

import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory
import com.parmet.squashlambdas.activity.Activity
import com.parmet.squashlambdas.activity.Clinic
import com.parmet.squashlambdas.activity.Court
import com.parmet.squashlambdas.activity.Match
import com.parmet.squashlambdas.activity.Sport
import com.parmet.squashlambdas.cal.Action
import java.lang.reflect.Type

internal object SportSerializer : JsonSerializer<Sport> {
    override fun serialize(
        src: Sport,
        typeOfSrc: Type,
        context: JsonSerializationContext,
    ) = JsonPrimitive(src.toString())
}

internal object CourtSerializer : JsonSerializer<Court> {
    override fun serialize(
        src: Court,
        typeOfSrc: Type,
        context: JsonSerializationContext,
    ) = JsonPrimitive("${src.pretty} (${src.sport})")
}

internal val ACTIVITY_ADAPTER_FACTORY =
    RuntimeTypeAdapterFactory.of(Activity::class.java)
        .registerSubtype(Match::class.java)
        .registerSubtype(Clinic::class.java)

internal object ActionSerializer : ClassNameSerializer<Action>()

internal abstract class ClassNameSerializer<T : Any> : JsonSerializer<T> {
    override fun serialize(
        src: T,
        typeOfSrc: Type,
        context: JsonSerializationContext,
    ) = JsonPrimitive(src::class.simpleName)
}
