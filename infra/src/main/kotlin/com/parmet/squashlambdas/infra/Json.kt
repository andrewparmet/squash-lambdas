package com.parmet.squashlambdas.infra

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal fun parseObject(value: String, description: String): JsonObject =
    runCatching { Json.parseToJsonElement(value).jsonObject }.getOrElse {
        error("Invalid $description JSON")
    }

internal fun JsonObject.requiredObject(name: String): JsonObject =
    getValue(name).jsonObject

internal fun JsonObject.requiredString(name: String): String =
    getValue(name).jsonPrimitive.content
