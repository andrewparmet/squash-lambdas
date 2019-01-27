package com.parmet.squashlambdas.notify

import com.fatboyindustrial.gsonjavatime.InstantConverter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory
import com.parmet.squashlambdas.activity.Activity
import com.parmet.squashlambdas.activity.Clinic
import com.parmet.squashlambdas.activity.Court
import com.parmet.squashlambdas.activity.Match
import com.parmet.squashlambdas.activity.Sport
import com.parmet.squashlambdas.cal.Action
import java.time.Instant

private val BASE_BUILDER =
    GsonBuilder().registerTypeAdapter(Instant::class.java, InstantConverter())

private val BASE_GSON =
    BASE_BUILDER.create()

internal val SPORT_ADAPTER =
    RuntimeTypeAdapterFactory.of(Sport::class.java)
        .registerSubtype(Sport.Squash::class.java)
        .registerSubtype(Sport.Hardball::class.java)
        .registerSubtype(Sport.Racquets::class.java)
        .registerSubtype(Sport.Tennis::class.java)
        .create(BASE_GSON, TypeToken.get(Sport::class.java))

internal val COURT_ADAPTER =
    RuntimeTypeAdapterFactory.of(Court::class.java)
        .registerSubtype(Court.Court1::class.java)
        .registerSubtype(Court.Court2::class.java)
        .registerSubtype(Court.Court3::class.java)
        .registerSubtype(Court.Court5::class.java)
        .registerSubtype(Court.Court6::class.java)
        .registerSubtype(Court.Court7::class.java)
        .registerSubtype(Court.RacquetsCourt::class.java)
        .registerSubtype(Court.TennisCourt::class.java)
        .create(
            BASE_BUILDER.registerTypeAdapter(Sport::class.java, SPORT_ADAPTER).create(),
            TypeToken.get(Court::class.java))

internal val ACTIVITY_ADAPTER =
    RuntimeTypeAdapterFactory.of(Activity::class.java)
        .registerSubtype(Match::class.java)
        .registerSubtype(Clinic::class.java)
        .create(
            BASE_BUILDER
                .registerTypeAdapter(Sport::class.java, SPORT_ADAPTER)
                .registerTypeAdapter(Court::class.java, COURT_ADAPTER)
                .create(),
            TypeToken.get(Activity::class.java))

internal val ACTION_ADAPTER =
    RuntimeTypeAdapterFactory.of(Action::class.java)
        .registerSubtype(Action.Create::class.java)
        .registerSubtype(Action.Delete::class.java)
        .registerSubtype(Action.Update::class.java)
        .registerSubtype(Action.None::class.java)
        .create(BASE_GSON, TypeToken.get(Action::class.java))
