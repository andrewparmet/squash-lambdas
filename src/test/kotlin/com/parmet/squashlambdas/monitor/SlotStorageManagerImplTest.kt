package com.parmet.squashlambdas.monitor

import com.parmet.squashlambdas.configureDynamoDb
import com.parmet.squashlambdas.testutil.ConfiguredTest
import org.junit.Ignore
import org.junit.Test
import java.time.LocalDate

class SlotStorageManagerImplTest : ConfiguredTest() {
    @Test
    @Ignore
    fun `test writing slots`() {
        client.startAsync().awaitRunning()
        val slots = client.slotsTaken(LocalDate.now(), LocalDate.now())

        SlotStorageManagerImpl(
            configureDynamoDb(),
            config.getString("aws.dynamo.squashSlotsTableName")
        ).save(LocalDate.now(), slots)
    }
}
