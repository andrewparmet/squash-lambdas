package com.parmet.squashlambdas.integration

import com.amazonaws.services.lambda.runtime.events.S3Event
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.Event
import com.google.common.truth.Truth.assertThat
import com.parmet.squashlambdas.EmailNotificationHandler
import com.parmet.squashlambdas.aws.ObjectStorage
import com.parmet.squashlambdas.aws.TopicPublisher
import com.parmet.squashlambdas.cal.CalendarProvider
import com.parmet.squashlambdas.cal.ChangeSummaryResolver
import com.parmet.squashlambdas.cal.ChangeSummaryTest
import com.parmet.squashlambdas.clublocker.StoredToken
import com.parmet.squashlambdas.json.Json
import com.parmet.squashlambdas.testutil.getResourceAsString
import dev.zacsweers.metro.createGraphFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Instant

class EmailNotificationHandlerTest {
    private val events = mockk<Calendar.Events>(relaxed = true)
    private val calendar = mockk<Calendar> { every { events() } returns events }
    private val calendarProvider = object : CalendarProvider {
        override suspend fun get() =
            calendar
    }
    private val identityChangeSummaryResolver = ChangeSummaryResolver { it }
    private val objectStorage = InMemoryObjectStorage()
    private val topicPublisher = RecordingTopicPublisher()

    @Test
    fun `react to a new reservation`() {
        objectStorage.objects["test-object-key"] =
            getResourceAsString(ChangeSummaryTest::class, "reservationCreated").encodeToByteArray()

        configureHandler().handleRequest(S3Event(listOf(createRecord())), mockk())

        val event = slot<Event>()
        verify { events.insert("primary", capture(event)) }
        assertThat(event.captured.start.dateTime.toString()).isEqualTo("2018-04-17T18:45:00.000-04:00")
        assertThat(event.captured.end.dateTime.toString()).isEqualTo("2018-04-17T19:30:00.000-04:00")
        assertThat(event.captured.location).isEqualTo("Court 2, Tennis and Racquet Club")
        assertThat(event.captured.summary).isEqualTo("Squash Match")
        assertThat(topicPublisher.messages.single().topicArn).isEqualTo("fake-arn")
        assertThat(topicPublisher.messages.single().message).contains("Successfully processed change")
    }

    @Test
    fun `react when the primary recipient is forwarded`() {
        objectStorage.objects["test-object-key"] =
            getResourceAsString(ChangeSummaryTest::class, "reservationCreated")
                .replace(Regex("(?m)^To: joecool@peanuts\\.com\\r?$"), "To: intermediate@example.com")
                .encodeToByteArray()

        configureHandler().handleRequest(S3Event(listOf(createRecord())), mockk())

        verify { events.insert("primary", any()) }
        assertThat(topicPublisher.messages).hasSize(1)
    }

    @Test
    fun `token update email stores token`() {
        objectStorage.objects["test-object-key"] =
            getResourceAsString(this::class, "tokenUpdateEmail").encodeToByteArray()

        configureHandler().handleRequest(S3Event(listOf(createRecord())), mockk())

        val storedToken: StoredToken = Json.decode(
            objectStorage.objects.getValue("clublocker-token.json").decodeToString()
        )
        assertThat(storedToken.token).isEqualTo("test-token-123")
        assertThat(topicPublisher.messages.single().subject).isEqualTo("ClubLocker token updated")
    }

    private fun createRecord() =
        S3EventNotification.S3EventNotificationRecord(
            "region",
            "eventName",
            "eventSource",
            Instant.now().toString(),
            "eventVersion",
            mockk(),
            mockk(),
            S3EventNotification.S3Entity(
                "configurationId",
                S3EventNotification.S3BucketEntity("test-bucket-name", mockk(), "arn"),
                S3EventNotification.S3ObjectEntity("test-object-key", 2319L, "eTag", "versionId", "sequencer"),
                "s3SchemaVersion",
            ),
            mockk(),
        )

    private fun configureHandler() =
        object : EmailNotificationHandler() {
            override fun buildGraph() =
                createGraphFactory<EmailNotificationTestGraph.Factory>().create(
                    "test-email-notification-handler.conf",
                    calendarProvider,
                    identityChangeSummaryResolver,
                    objectStorage,
                    topicPublisher,
                )
        }
}

private class InMemoryObjectStorage : ObjectStorage {
    val objects = mutableMapOf<String, ByteArray>()

    override suspend fun read(bucket: String, key: String) =
        objects.getValue(key)

    override suspend fun write(bucket: String, key: String, contents: ByteArray) {
        objects[key] = contents
    }

    override suspend fun eTag(bucket: String, key: String) =
        objects.getValue(key).contentHashCode().toString()
}

private class RecordingTopicPublisher : TopicPublisher {
    val messages = mutableListOf<PublishedMessage>()

    override suspend fun publish(topicArn: String, subject: String, message: String) {
        messages += PublishedMessage(topicArn, subject, message)
    }
}

private data class PublishedMessage(
    val topicArn: String,
    val subject: String,
    val message: String
)
