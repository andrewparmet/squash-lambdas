package com.parmet.squashlambdas.reserve

import com.fatboyindustrial.gsonjavatime.InstantConverter
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.parmet.squashlambdas.BOSTON
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

internal object InputParser {
    private val gson =
        GsonBuilder()
            .registerTypeAdapter(Instant::class.java, InstantConverter())
            .create()

    fun parseRequestDate(input: Any): LocalDate =
        gson.fromJson(gson.toJsonTree(input), Input::class.java)
            .time
            .atZone(BOSTON)
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
