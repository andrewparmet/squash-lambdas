package com.parmet.squashlambdas.monitor

import com.parmet.squashlambdas.testutil.ConfiguredTest
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.time.LocalDate

class SlotStorageManagerImplTest : ConfiguredTest() {
    @Test
    @Disabled
    fun `test writing slots`() {
        client.init()
        val slots = client.slotsTaken(LocalDate.now(), LocalDate.now())

        SlotStorageManagerImpl(
            DynamoDbClient.create(),
            monitorSlotsConfig.dynamoDb.squashSlotsTableName
        ).save(LocalDate.now(), slots)
    }
}
