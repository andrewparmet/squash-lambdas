package com.parmet.squashlambdas.monitor

import com.parmet.squashlambdas.configureDynamoDb
import com.parmet.squashlambdas.testutil.ConfiguredTest
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.LocalDate

class SlotStorageManagerImplTest : ConfiguredTest() {
    @Test
    @Disabled
    fun `test writing slots`() {
        client.startAsync().awaitRunning()
        val slots = client.slotsTaken(LocalDate.now(), LocalDate.now())

        SlotStorageManagerImpl(
            configureDynamoDb(),
            config.getString("aws.dynamo.squashSlotsTableName"),
        ).save(LocalDate.now(), slots)
    }
}
