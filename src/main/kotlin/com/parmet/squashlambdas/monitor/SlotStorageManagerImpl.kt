package com.parmet.squashlambdas.monitor

import com.google.gson.Gson
import com.parmet.squashlambdas.clublocker.Slot
import io.github.oshai.kotlinlogging.KotlinLogging
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import java.time.Instant
import java.time.LocalDate

internal class SlotStorageManagerImpl(
    private val dynamoDb: DynamoDbClient,
    private val tableName: String
) : SlotStorageManager {
    private val logger = KotlinLogging.logger { }
    private val gson = Gson()

    private val primaryKey = "filename"
    private val entriesKey = "entries"
    private val modifiedTimeKey = "modifiedTime"

    override fun save(date: LocalDate, slots: List<Slot>) {
        val item: MutableMap<String, AttributeValue> = mutableMapOf()
        item[primaryKey] = AttributeValue.builder().s("$date/taken").build()
        item[entriesKey] = AttributeValue.builder().ss(slots.map(gson::toJson)).build()
        item[modifiedTimeKey] = AttributeValue.builder().s(Instant.now().toString()).build()

        dynamoDb.putItem(
            PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build()
        )

        logger.info { "Saved latest slots taken for $date: $slots" }
    }

    override fun loadLatest(date: LocalDate): List<Slot> {
        val keyMap: MutableMap<String, AttributeValue> = mutableMapOf()
        keyMap[primaryKey] = AttributeValue.builder().s("$date/taken").build()
        val response = dynamoDb.getItem(
            GetItemRequest.builder()
                .tableName(tableName)
                .key(keyMap)
                .build()
        )
        return if (response.item().isEmpty()) {
            emptyList()
        } else {
            val jsonEntries: List<String> = response.item()[entriesKey]?.ss() ?: emptyList()
            jsonEntries.map { json -> gson.fromJson(json, Slot::class.java) }
                .also { logger.info { "Loaded latest snapshot of taken slots: $it" } }
        }
    }
}
