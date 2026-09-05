package com.parmet.squashlambdas.monitor

import com.google.common.truth.Truth.assertThat
import com.parmet.squashlambdas.DynamoDbConfig
import com.parmet.squashlambdas.clublocker.Slot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse
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

    @Test
    fun `saving no slots omits the empty string set`() {
        val request = slot<PutItemRequest>()

        manager.save(LocalDate.of(2026, 9, 5), emptyList())

        verify { dynamoDb.putItem(capture(request)) }
        assertThat(request.captured.item()).doesNotContainKey("entries")
    }

    @Test
    fun `missing snapshot loads as no slots`() {
        every { dynamoDb.getItem(any<GetItemRequest>()) } returns GetItemResponse.builder().item(emptyMap()).build()

        assertThat(manager.loadLatest(LocalDate.of(2026, 9, 5))).isEmpty()
    }

    @Test
    fun `snapshot without entries loads as no slots`() {
        every { dynamoDb.getItem(any<GetItemRequest>()) } returns
            GetItemResponse.builder().item(
                mapOf("filename" to AttributeValue.builder().s("2026-09-05/taken").build())
            ).build()

        assertThat(manager.loadLatest(LocalDate.of(2026, 9, 5))).isEmpty()
    }

    @Test
    fun `saved slots round trip`() {
        val date = LocalDate.of(2026, 9, 5)
        val slots = listOf(Slot(1, 2, 3, 1800, 1845, 1788645600))
        val request = slot<PutItemRequest>()
        manager.save(date, slots)
        verify { dynamoDb.putItem(capture(request)) }
        every { dynamoDb.getItem(any<GetItemRequest>()) } returns
            GetItemResponse.builder().item(request.captured.item()).build()

        assertThat(manager.loadLatest(date)).containsExactlyElementsIn(slots)
    }
}
