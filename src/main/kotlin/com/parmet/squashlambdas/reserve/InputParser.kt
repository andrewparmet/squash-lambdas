package com.parmet.squashlambdas.reserve

import com.fasterxml.jackson.annotation.JsonProperty
import com.parmet.squashlambdas.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

internal object InputParser {
    private val mapper = Json.mapper

    fun parseRequestDate(input: Any) =
        parseRequestDate(mapper.convertValue(input, Input::class.java))

    internal fun parseRequestDate(input: Input): LocalDate =
        input.time
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
            .plusDays(7)

    internal data class Input(
        val version: String,
        val id: UUID,
        @param:JsonProperty("detail-type")
        val detailType: String,
        val source: String,
        val account: String,
        val time: Instant,
        val region: String,
        val resources: List<String>,
        val detail: Map<String, Any>
    )
}
