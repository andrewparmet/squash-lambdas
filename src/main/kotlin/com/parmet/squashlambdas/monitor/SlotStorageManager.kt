package com.parmet.squashlambdas.monitor

import com.fasterxml.jackson.module.kotlin.readValue
import com.parmet.squashlambdas.DynamoDbConfig
import com.parmet.squashlambdas.clublocker.Slot
import com.parmet.squashlambdas.json.Json
import io.github.oshai.kotlinlogging.KotlinLogging
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

class SlotStorageManager @Inject constructor(
    private val dynamoDb: DynamoDbClient,
    private val config: DynamoDbConfig
) {
    private val logger = KotlinLogging.logger { }
    private val primaryKey = "filename"
    private val entriesKey = "entries"
    private val modifiedTimeKey = "modifiedTime"

    fun save(date: LocalDate, slots: List<Slot>) {
        val item: MutableMap<String, AttributeValue> = mutableMapOf()
        item[primaryKey] = AttributeValue.builder().s("$date/taken").build()
        item[entriesKey] = AttributeValue.builder().ss(slots.map { Json.mapper.writeValueAsString(it) }).build()
        item[modifiedTimeKey] = AttributeValue.builder().s(Instant.now().toString()).build()

        dynamoDb.putItem(
            PutItemRequest.builder()
                .tableName(config.squashSlotsTableName)
                .item(item)
                .build()
        )

        logger.info { "Saved latest slots taken for $date: $slots" }
    }

    fun loadLatest(date: LocalDate): List<Slot> {
        val keyMap: MutableMap<String, AttributeValue> = mutableMapOf()
        keyMap[primaryKey] = AttributeValue.builder().s("$date/taken").build()
        val response = dynamoDb.getItem(
            GetItemRequest.builder()
                .tableName(config.squashSlotsTableName)
                .key(keyMap)
                .build()
        )
        return if (response.item().isEmpty()) {
            emptyList()
        } else {
            val jsonEntries: List<String> = response.item()[entriesKey]?.ss() ?: emptyList()
            jsonEntries.map { json -> Json.mapper.readValue<Slot>(json) }
                .also { logger.info { "Loaded latest snapshot of taken slots: $it" } }
        }
    }
}
