package com.parmet.squashlambdas.monitor

import com.parmet.squashlambdas.configureDynamoDb
import com.parmet.squashlambdas.testutil.ConfiguredTest
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.LocalDate

class SlotsTrackerTest : ConfiguredTest() {
    @Test
    @Disabled
    fun `test finding newly open slots`() {
        client.startAsync().awaitRunning()

        val dynamoClient =
            SlotStorageManagerImpl(
                configureDynamoDb(),
                config.getString("aws.dynamo.squashSlotsTableName"),
            )

        println(SlotsTracker(client, dynamoClient).findNewlyOpen(LocalDate.now()))
    }
}
