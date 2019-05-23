package com.parmet.squashlambdas.notify

import com.amazonaws.services.sns.AbstractAmazonSNS
import com.amazonaws.services.sns.model.PublishRequest
import com.amazonaws.services.sns.model.PublishResult
import com.google.common.truth.Truth.assertThat
import com.parmet.squashlambdas.activity.Court
import com.parmet.squashlambdas.activity.Match
import com.parmet.squashlambdas.activity.Player
import com.parmet.squashlambdas.cal.Action
import com.parmet.squashlambdas.cal.ChangeSummary
import mu.KotlinLogging
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset

class NotifierTest {
    private val logger = KotlinLogging.logger { }

    private val received = mutableListOf<PublishRequest>()

    private val sns = object : AbstractAmazonSNS() {
        override fun publish(req: PublishRequest): PublishResult {
            received.add(req)
            return PublishResult()
        }
    }

    private val notifier = Notifier(sns, "some-arn", "another-arn")

    @Test
    fun `notifier sends a reasonable message on success`() {
        notifier.publishSuccessfulParse(
            ChangeSummary(
                Action.Create,
                Match(Court.Court1, Instant.now(), Instant.now(), setOf(Player.named("Andrew Parmet")))))

        logger.info { "Received ${received[0].message}" }

        assertThat(received).hasSize(1)
        assertThat(received[0].topicArn).isEqualTo("some-arn")
        assertThat(received[0].subject).isEqualTo("Processed Club Locker Email")
        assertThat(received[0].message).contains("Andrew Parmet")
        assertThat(received[0].message).contains("Court 1")
        assertThat(received[0].message).contains("Squash")
        assertThat(received[0].message).contains(Instant.now().atZone(ZoneOffset.UTC).toLocalDate().toString())
    }

    @Test
    fun `notifier sends a reasonable message on failure`() {
        notifier.publishFailedParse(
            ExceptionInInitializerError("something terrible has happened"),
            mapOf("key123" to "val456"))

        logger.info { "Received ${received[0].message}" }

        assertThat(received).hasSize(1)
        assertThat(received[0].topicArn).isEqualTo("some-arn")
        assertThat(received[0].subject).isEqualTo("Failed to Process Club Locker Email")
        assertThat(received[0].message).contains("ExceptionInInitializerError")
        assertThat(received[0].message).contains("key123")
        assertThat(received[0].message).contains("val456")
    }
}
