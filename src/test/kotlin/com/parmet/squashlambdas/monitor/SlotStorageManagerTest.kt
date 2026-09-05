package com.parmet.squashlambdas.monitor

import com.google.common.truth.Truth.assertThat
import com.parmet.squashlambdas.DynamoDbConfig
import com.parmet.squashlambdas.clublocker.Slot
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import java.time.LocalDate

class SlotStorageManagerTest {
    private val dynamoDb = mockk<DynamoDbClient>(relaxed = true)
    private val manager = SlotStorageManager(dynamoDb, DynamoDbConfig("slots-table"))

    @Test
    fun `saved slots expire after their date`() {
        val request = slot<PutItemRequest>()

        manager.save(
            LocalDate.of(2026, 9, 5),
            listOf(Slot(1, 2, 3, 1800, 1845, 1788645600))
        )

        verify { dynamoDb.putItem(capture(request)) }
        assertThat(request.captured.item()["ttl"]?.n()).isEqualTo("1788667200")
    }
}
