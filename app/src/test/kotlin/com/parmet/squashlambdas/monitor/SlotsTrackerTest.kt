package com.parmet.squashlambdas.monitor

import com.parmet.squashlambdas.aws.DynamoDbMapperProvider
import com.parmet.squashlambdas.clublocker.TokenStatusManager
import com.parmet.squashlambdas.testutil.ConfiguredTest
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.LocalDate

class SlotsTrackerTest : ConfiguredTest() {
    @Test
    @Disabled
    fun `test finding newly open slots`() =
        runTest {
            client.init()
            val table = SlotSnapshotTable(DynamoDbMapperProvider(), monitorSlotsConfig.dynamoDb)
            val tokenStatusManager = mockk<TokenStatusManager>(relaxed = true)

            println(SlotsTracker(client, SlotStorageManager(table), tokenStatusManager).findNewlyOpen(LocalDate.now()))
        }
}
