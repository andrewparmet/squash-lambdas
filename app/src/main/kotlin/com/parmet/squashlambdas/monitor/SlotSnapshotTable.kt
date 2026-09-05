package com.parmet.squashlambdas.monitor

import aws.sdk.kotlin.hll.dynamodbmapper.DynamoDbItem
import aws.sdk.kotlin.hll.dynamodbmapper.DynamoDbPartitionKey
import aws.sdk.kotlin.hll.dynamodbmapper.operations.getItem
import aws.sdk.kotlin.hll.dynamodbmapper.operations.putItem
import com.parmet.squashlambdas.DynamoDbConfig
import com.parmet.squashlambdas.aws.DynamoDbMapperProvider
import com.parmet.squashlambdas.monitor.dynamodbmapper.generatedschemas.getSlotSnapshotTable
import dev.zacsweers.metro.Inject

@DynamoDbItem
data class SlotSnapshot(
    @DynamoDbPartitionKey
    val filename: String,
    val entries: List<String>,
    val modifiedTime: String,
    val ttl: Long
)

@Inject
class SlotSnapshotTable(
    private val mapperProvider: DynamoDbMapperProvider,
    private val config: DynamoDbConfig
) {
    suspend fun put(snapshot: SlotSnapshot) {
        table().putItem { item = snapshot }
    }

    suspend fun get(filename: String): SlotSnapshot? =
        table().getItem(filename).item

    private suspend fun table() =
        mapperProvider.get().getSlotSnapshotTable(config.squashSlotsTableName)
}
