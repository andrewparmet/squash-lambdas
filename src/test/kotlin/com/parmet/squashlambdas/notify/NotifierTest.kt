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
import com.parmet.squashlambdas.clublocker.Slot
import mu.KotlinLogging
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneOffset

class NotifierTest {
    private val logger = KotlinLogging.logger { }

    private val received = mutableListOf<PublishRequest>()

    private val sns =
        object : AbstractAmazonSNS() {
            override fun publish(req: PublishRequest): PublishResult {
                received.add(req)
                return PublishResult()
            }
        }

    private val context = mutableMapOf<Any, Any>()
    private val notifier = Notifier(sns, "some-arn", context)

    @Test
    fun `notifier sends a reasonable message on success`() {
        notifier.publishSuccessfulParse(
            ChangeSummary(
                Action.Create,
                Match(
                    Court.Court1,
                    Instant.now(),
                    Instant.now(),
                    "",
                    setOf(Player(name = "Andrew Parmet"))
                )
            )
        )

        logger.info { "Received ${received[0].message}" }

        assertThat(received).hasSize(1)
        assertThat(received[0].topicArn).isEqualTo("some-arn")
        assertThat(received[0].subject).isEqualTo("Processed: Squash Match")
        assertThat(received[0].message).contains("Andrew Parmet")
        assertThat(received[0].message).contains("Court 1")
        assertThat(received[0].message).contains("Squash")
        assertThat(received[0].message).contains(Instant.now().atZone(ZoneOffset.UTC).toLocalDate().toString())
    }

    @Test
    fun `notifier sends a reasonable message on failure`() {
        context["key123"] = "val456"
        notifier.publishFailedParse(ExceptionInInitializerError("something terrible has happened"))

        logger.info { "Received ${received[0].message}" }

        assertThat(received).hasSize(1)
        assertThat(received[0].topicArn).isEqualTo("some-arn")
        assertThat(received[0].subject).isEqualTo("Failed to Process Club Locker Email")
        assertThat(received[0].message).contains("ExceptionInInitializerError")
        assertThat(received[0].message).contains("key123")
        assertThat(received[0].message).contains("val456")
    }

    @Test
    fun `notifier sends a reasonable message on success monitoring slots`() {
        notifier.publishFoundOpenSlot(listOf(Slot(1, 1, 1411, 1, 1, Instant.parse("2019-06-01T00:31:31Z").epochSecond)))

        logger.info { "Received ${received[0].message}" }

        assertThat(received).hasSize(1)
        assertThat(received[0].topicArn).isEqualTo("some-arn")
        assertThat(received[0].subject).contains("Found new open slots on Club Locker")
        assertThat(received[0].message).contains("Friday, May 31: Court 1, 0:01 am-0:01 am")
    }
}
