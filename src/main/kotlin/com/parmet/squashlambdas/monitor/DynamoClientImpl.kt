package com.parmet.squashlambdas.monitor

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.google.gson.Gson
import com.parmet.squashlambdas.clublocker.Slot
import mu.KotlinLogging
import java.time.Instant
import java.time.LocalDate

internal class DynamoClientImpl(
    private val dynamoDb: AmazonDynamoDB,
    private val tableName: String
) : DynamoClient {
    private val logger = KotlinLogging.logger { }
    private val gson = Gson()

    private val primaryKey = "filename"
    private val entriesKey = "entries"

    override fun save(date: LocalDate, slots: List<Slot>) {
        val now = Instant.now()
        dynamoDb.putItem(
            tableName,
            mapOf(
                primaryKey to AttributeValue("$date/$now"),
                entriesKey to AttributeValue(slots.map(gson::toJson))
            )
        )

        val index = loadIndex(date)
        saveIndex(index.copy(entries = index.entries + now))

        logger.info { "Saved latest slots taken for $date: $slots" }
    }

    override fun loadLatest(date: LocalDate): List<Slot> {
        val latest = loadIndex(date).entries.max()
        val item = dynamoDb.getItem(tableName, mapOf(primaryKey to AttributeValue("$date/$latest")))
        return item.item.getValue(entriesKey).ss.map { gson.fromJson(it, Slot::class.java) }
            .also { logger.info { "Loaded latest snapshot of taken slots: $it" } }
    }

    private fun loadIndex(date: LocalDate): Index {
        val item = dynamoDb.getItem(tableName, mapOf(primaryKey to AttributeValue(Index.key(date))))
        return if (item.item == null) {
            Index(date, emptySet())
        } else {
            Index(
                date,
                item.item.getValue(entriesKey).ss.map { Instant.parse(it) }.toSortedSet()
            )
        }.also {
            logger.info { "Loaded latest index: $it" }
        }
    }

    private fun saveIndex(index: Index) {
        logger.info { "Saving index $index" }
        dynamoDb.putItem(
            tableName,
            mapOf(
                primaryKey to AttributeValue(index.fileName),
                entriesKey to AttributeValue(index.entries.map(Instant::toString))
            )
        )
    }

    private data class Index(
        val date: LocalDate,
        val entries: Set<Instant>
    ) {
        val fileName
            get() = key(date)

        companion object {
            fun key(date: LocalDate) =
                "$date-index"
        }
    }
}
