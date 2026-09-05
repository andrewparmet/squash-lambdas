package com.parmet.squashlambdas.integration

import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.Event
import com.google.common.truth.Truth.assertThat
import com.parmet.squashlambdas.EmailNotificationConfig
import com.parmet.squashlambdas.EmailNotificationHandler
import com.parmet.squashlambdas.EmailRoutingConfig
import com.parmet.squashlambdas.EmailTenantConfig
import com.parmet.squashlambdas.aws.ObjectStorage
import com.parmet.squashlambdas.aws.TopicPublisher
import com.parmet.squashlambdas.cal.CalendarProvider
import com.parmet.squashlambdas.cal.ChangeSummaryResolver
import com.parmet.squashlambdas.cal.ChangeSummaryTest
import com.parmet.squashlambdas.clublocker.StoredToken
import com.parmet.squashlambdas.email.SesEmailEvent
import com.parmet.squashlambdas.email.SesEmailRecord
import com.parmet.squashlambdas.email.SesMail
import com.parmet.squashlambdas.email.SesMessage
import com.parmet.squashlambdas.email.SesReceipt
import com.parmet.squashlambdas.email.SesVerdict
import com.parmet.squashlambdas.json.Json
import com.parmet.squashlambdas.testutil.getResourceAsString
import dev.zacsweers.metro.createGraphFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.io.OutputStream

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
    private val routingConfig =
        EmailRoutingConfig(
            bucket = "test-bucket-name",
            inboundEmailPrefix = "inbound",
            inboundRecipient = "receiver@example.com",
            clubLockerEmail = "joecool@peanuts.com",
            clubLockerTokenKey = "clublocker-token.json",
            googleCalendarCredentialsKey = "google-credentials.json",
            notificationTopicArn = "fake-arn",
            calendarExpectedSender = "archived-contact@example.com",
            tokenUpdateExpectedSender = "joecool@peanuts.com",
            tokenUpdateExpectedSubject = "ClubLocker Token",
            tenants =
            mapOf(
                "primary" to
                    EmailTenantConfig(
                        forwardedRecipient = "joecool@peanuts.com",
                        googleCalendarId = "primary"
                    ),
                "secondary" to
                    EmailTenantConfig(
                        forwardedRecipient = "second-user@example.com",
                        googleCalendarId = "secondary"
                    )
            )
        )

    @Test
    fun `react to a new reservation`() {
        objectStorage.objects["inbound/test-object-key"] =
            getResourceAsString(ChangeSummaryTest::class, "reservationCreated").encodeToByteArray()

        handle()

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
    fun `ignore X-Forwarded-To when routing`() {
        objectStorage.objects["inbound/test-object-key"] =
            getResourceAsString(ChangeSummaryTest::class, "reservationCreated")
                .replace(Regex("(?m)^To: joecool@peanuts\\.com\\r?$"), "To: intermediate@example.com")
                .encodeToByteArray()

        handle()

        verify(exactly = 0) { events.insert(any(), any()) }
        assertThat(topicPublisher.messages).hasSize(1)
        assertThat(topicPublisher.messages.single().subject).isEqualTo("Failed to Execute Club Locker Lambda")
    }

    @Test
    fun `react to every record in an SES event`() {
        val email = getResourceAsString(ChangeSummaryTest::class, "reservationCreated").encodeToByteArray()
        objectStorage.objects["inbound/first-object-key"] = email
        objectStorage.objects["inbound/second-object-key"] = email

        handle(createRecord("first-object-key"), createRecord("second-object-key"))

        verify(exactly = 2) { events.insert("primary", any()) }
        assertThat(topicPublisher.messages).hasSize(2)
    }

    @Test
    fun `route by the original recipient`() {
        objectStorage.objects["inbound/second-object-key"] =
            getResourceAsString(ChangeSummaryTest::class, "reservationCreated")
                .replace("joecool@peanuts.com", "second-user@example.com")
                .encodeToByteArray()

        handle(createRecord("second-object-key"))

        assertThat(objectStorage.readKeys).containsExactly("inbound/second-object-key")
        verify { events.insert("secondary", any()) }
    }

    @Test
    fun `token update email stores token`() {
        objectStorage.objects["inbound/test-object-key"] =
            getResourceAsString(this::class, "tokenUpdateEmail").encodeToByteArray()

        handle()

        val storedToken: StoredToken = Json.decode(
            objectStorage.objects.getValue("clublocker-token.json").decodeToString()
        )
        assertThat(storedToken.token).isEqualTo("test-token-123")
        assertThat(topicPublisher.messages.single().subject).isEqualTo("ClubLocker token updated")
    }

    @Test
    fun `reject unauthenticated token update without publishing its body`() {
        objectStorage.objects["inbound/test-object-key"] =
            getResourceAsString(this::class, "tokenUpdateEmail").encodeToByteArray()

        handle(createRecord(dmarcStatus = "FAIL"))

        assertThat(objectStorage.objects).doesNotContainKey("clublocker-token.json")
        assertThat(topicPublisher.messages.single().message).doesNotContain("test-token-123")
    }

    @Test
    fun `reject unsafe mail before retrieving its body`() {
        handle(createRecord(virusStatus = "FAIL"))

        assertThat(objectStorage.readKeys).isEmpty()
        verify(exactly = 0) { events.insert(any(), any()) }
        assertThat(topicPublisher.messages).hasSize(1)
    }

    @Test
    fun `reject an unexpected SES recipient before retrieving its body`() {
        handle(createRecord(recipients = listOf("unexpected@example.com")))

        assertThat(objectStorage.readKeys).isEmpty()
        verify(exactly = 0) { events.insert(any(), any()) }
        assertThat(topicPublisher.messages).hasSize(1)
    }

    @Test
    fun `accept an authenticated forwarded calendar email without DMARC alignment`() {
        objectStorage.objects["inbound/test-object-key"] =
            getResourceAsString(ChangeSummaryTest::class, "reservationCreated").encodeToByteArray()

        handle(createRecord(dmarcStatus = "FAIL"))

        verify { events.insert("primary", any()) }
        assertThat(topicPublisher.messages.single().subject).isEqualTo("Processed: Squash Match")
    }

    @Test
    fun `reject a calendar email that fails DKIM`() {
        objectStorage.objects["inbound/test-object-key"] =
            getResourceAsString(ChangeSummaryTest::class, "reservationCreated").encodeToByteArray()

        handle(createRecord(dkimStatus = "FAIL"))

        verify(exactly = 0) { events.insert(any(), any()) }
        assertThat(topicPublisher.messages.single().subject).isEqualTo("Failed to Execute Club Locker Lambda")
    }

    @Test
    fun `notify for an unroutable record and continue`() {
        val email = getResourceAsString(ChangeSummaryTest::class, "reservationCreated")
        objectStorage.objects["inbound/unroutable"] =
            email.replace(Regex("(?m)^To: joecool@peanuts\\.com\\r?$"), "To: unknown@example.com").encodeToByteArray()
        objectStorage.objects["inbound/test-object-key"] = email.encodeToByteArray()

        handle(
            createRecord("unroutable"),
            createRecord()
        )

        verify(exactly = 1) { events.insert("primary", any()) }
        assertThat(topicPublisher.messages.map { it.subject })
            .containsExactly("Failed to Execute Club Locker Lambda", "Processed: Squash Match")
            .inOrder()
    }

    private fun handle(vararg records: SesEmailRecord = arrayOf(createRecord())) {
        val input = Json.encode(SesEmailEvent(records.toList())).byteInputStream()
        configureHandler().handleRequest(input, OutputStream.nullOutputStream(), mockk())
    }

    private fun createRecord(
        messageId: String = "test-object-key",
        recipients: List<String> = listOf("receiver@example.com"),
        dkimStatus: String = "PASS",
        dmarcStatus: String = "PASS",
        spamStatus: String = "PASS",
        virusStatus: String = "PASS"
    ) =
        SesEmailRecord(
            SesMessage(
                SesMail(messageId),
                SesReceipt(
                    recipients,
                    SesVerdict(spamStatus),
                    SesVerdict(virusStatus),
                    SesVerdict(dkimStatus),
                    SesVerdict(dmarcStatus)
                )
            )
        )

    private fun configureHandler() =
        object : EmailNotificationHandler() {
            override fun buildObjectStorage() =
                objectStorage

            override suspend fun loadRoutingConfig(objectStorage: ObjectStorage) =
                routingConfig

            override fun buildGraph(config: EmailNotificationConfig) =
                createGraphFactory<EmailNotificationTestGraph.Factory>().create(
                    config,
                    calendarProvider,
                    identityChangeSummaryResolver,
                    objectStorage,
                    topicPublisher,
                )
        }
}

private class InMemoryObjectStorage : ObjectStorage {
    val objects = mutableMapOf<String, ByteArray>()
    val readKeys = mutableListOf<String>()

    override suspend fun read(bucket: String, key: String): ByteArray {
        readKeys += key
        return objects.getValue(key)
    }

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
