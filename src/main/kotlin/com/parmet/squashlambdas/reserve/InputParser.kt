package com.parmet.squashlambdas.reserve

import com.fatboyindustrial.gsonjavatime.InstantConverter
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

internal object InputParser {
    private val gson =
        GsonBuilder()
            .registerTypeAdapter(Instant::class.java, InstantConverter())
            .create()

    fun parseRequestDate(input: Any): LocalDate =
        parseRequestDate(gson.fromJson(gson.toJsonTree(input), Input::class.java))

    internal fun parseRequestDate(input: Input): LocalDate =
        input.time
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
            .plusDays(7)

    internal data class Input(
        val version: String,
        val id: UUID,
        @SerializedName("detail-type")
        val detailType: String,
        val source: String,
        val account: String,
        val time: Instant,
        val region: String,
        val resources: List<String>,
        val detail: Map<String, Any>
    )
}
