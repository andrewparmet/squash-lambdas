package com.parmet.squashlambdas.integration

import com.amazonaws.services.lambda.runtime.events.S3Event
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.Event
import com.google.common.truth.Truth.assertThat
import com.parmet.squashlambdas.EmailNotificationHandler
import com.parmet.squashlambdas.cal.ChangeSummaryTest
import com.parmet.squashlambdas.testutil.getResourceAsString
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verifySequence
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.PublishRequest
import java.time.Instant

@Testcontainers
class EmailNotificationHandlerTest {
    private val events = mockk<Calendar.Events>(relaxed = true)
    private val calender = mockk<Calendar> { every { events() } returns events }
    private val snsClient = mockk<SnsClient>(relaxed = true)

    @Container
    val localstack =
        LocalStackContainer(DockerImageName.parse("localstack/localstack:4.7.0"))
            .withServices(LocalStackContainer.Service.S3)

    private val s3Client by lazy {
        S3Client.builder()
            .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.S3))
            .region(Region.of(localstack.region))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(localstack.accessKey, localstack.secretKey)
                )
            )
            .serviceConfiguration(
                S3Configuration.builder().pathStyleAccessEnabled(true).build()
            )
            .build()
    }

    @BeforeEach
    fun before() {
        Thread.sleep(2000) // localstack isn't ready yet and not sure how to get it to wait
        s3Client.createBucket {
            it.bucket("test-bucket-name")
        }
    }

    @AfterEach
    fun after() {
        confirmVerified(events, snsClient)
    }

    @Test
    fun `react to a new reservation`() {
        val handler = configureHandler()
        s3Client.putObject(
            {
                it.bucket("test-bucket-name")
                it.key("test-object-key")
            },
            RequestBody.fromString(getResourceAsString(ChangeSummaryTest::class, "reservationCreated"))
        )

        handler.handleRequest(S3Event(listOf(createRecord())), mockk())

        val event = slot<Event>()
        val publishRequest = slot<PublishRequest>()

        verifySequence {
            events.insert("primary", capture(event))
            snsClient.publish(capture(publishRequest))
        }

        assertThat(event.captured.start.dateTime.toString()).isEqualTo("2018-04-17T18:45:00.000-04:00")
        assertThat(event.captured.end.dateTime.toString()).isEqualTo("2018-04-17T19:30:00.000-04:00")
        assertThat(event.captured.location).isEqualTo("Court 2, Tennis and Racquet Club")
        assertThat(event.captured.summary).isEqualTo("Squash Match")

        assertThat(publishRequest.captured.topicArn()).isEqualTo("fake-arn")
        assertThat(publishRequest.captured.message()).startsWith(
            """
                Successfully processed change:
                {
                  "action": "Create",
                  "activity": {
                    "type": "Match",
                    "court": "Court 2 (Squash)",
                    "start": "2018-04-17T22:45:00Z",
                    "end": "2018-04-17T23:30:00Z",
                    "origin": "test-object-key",
                    "players": []
                  }
                }
            """.trimIndent()
        )
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
                S3EventNotification.S3BucketEntity(
                    "test-bucket-name",
                    mockk(),
                    "arn"
                ),
                S3EventNotification.S3ObjectEntity(
                    "test-object-key",
                    2319L,
                    "eTag",
                    "versionId",
                    "sequencer"
                ),
                "s3SchemaVersion"
            ),
            mockk()
        )

    private fun configureHandler(): EmailNotificationHandler {
        val handler = object : EmailNotificationHandler() {
            override fun buildComponent() =
                DaggerEmailNotificationTestComponent
                    .builder()
                    .configName("test-email-notification-handler.yml")
                    .emailNotificationTestModule(EmailNotificationTestModule(calender, s3Client, snsClient))
                    .build()
        }

        return handler
    }
}
