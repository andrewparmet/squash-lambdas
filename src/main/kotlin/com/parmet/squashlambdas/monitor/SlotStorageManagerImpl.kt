package com.parmet.squashlambdas.monitor

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.google.gson.Gson
import com.parmet.squashlambdas.clublocker.Slot
import mu.KotlinLogging
import java.time.Instant
import java.time.LocalDate

internal class SlotStorageManagerImpl(
    private val dynamoDb: AmazonDynamoDB,
    private val tableName: String,
) : SlotStorageManager {
    private val logger = KotlinLogging.logger { }
    private val gson = Gson()

    private val primaryKey = "filename"
    private val entriesKey = "entries"
    private val modifiedTimeKey = "modifiedTime"

    override fun save(
        date: LocalDate,
        slots: List<Slot>,
    ) {
        dynamoDb.putItem(
            tableName,
            mapOf(
                primaryKey to key(date),
                entriesKey to AttributeValue(slots.map(gson::toJson)),
                modifiedTimeKey to AttributeValue(Instant.now().toString()),
            ),
        )

        logger.info { "Saved latest slots taken for $date: $slots" }
    }

    override fun loadLatest(date: LocalDate): List<Slot> {
        val item = dynamoDb.getItem(tableName, mapOf(primaryKey to key(date)))
        return if (item.item == null) {
            emptyList()
        } else {
            item.item.getValue(entriesKey).ss.map { gson.fromJson(it, Slot::class.java) }
                .also { logger.info { "Loaded latest snapshot of taken slots: $it" } }
        }
    }

    private fun key(date: LocalDate) = AttributeValue("$date/taken")
}
