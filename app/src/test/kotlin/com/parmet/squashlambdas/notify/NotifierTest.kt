package com.parmet.squashlambdas.notify

import com.google.common.truth.Truth.assertThat
import com.parmet.squashlambdas.activity.Court
import com.parmet.squashlambdas.activity.Match
import com.parmet.squashlambdas.activity.Player
import com.parmet.squashlambdas.aws.TopicPublisher
import com.parmet.squashlambdas.cal.Action
import com.parmet.squashlambdas.cal.ChangeSummary
import com.parmet.squashlambdas.clublocker.Slot
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneOffset

class NotifierTest {
    private val received = mutableListOf<PublishedMessage>()
    private val publisher =
        object : TopicPublisher {
            override suspend fun publish(topicArn: String, subject: String, message: String) {
                received += PublishedMessage(topicArn, subject, message)
            }
        }
    private val context = mutableMapOf<String, JsonElement>()
    private val notifier = Notifier(publisher, "some-arn", context)

    @Test
    fun `notifier sends a reasonable message on success`() =
        runTest {
            notifier.publishSuccessfulParse(
                ChangeSummary(
                    Action.Create,
                    Match(Court.Court1, Instant.now(), Instant.now(), "", setOf(Player(name = "Opponent Player")))
                )
            )

            assertThat(received).hasSize(1)
            assertThat(received[0].topicArn).isEqualTo("some-arn")
            assertThat(received[0].subject).isEqualTo("Processed: Squash v. Opponent Player")
            assertThat(received[0].message).contains("Opponent Player")
            assertThat(received[0].message).contains("Court 1")
            assertThat(received[0].message).contains("Squash")
            assertThat(received[0].message).contains(Instant.now().atZone(ZoneOffset.UTC).toLocalDate().toString())
        }

    @Test
    fun `notifier sends a reasonable message on failure`() =
        runTest {
            context["key123"] = JsonPrimitive("val456")
            notifier.publishFailure(ExceptionInInitializerError("something terrible has happened"))

            assertThat(received).hasSize(1)
            assertThat(received[0].topicArn).isEqualTo("some-arn")
            assertThat(received[0].subject).isEqualTo("Failed to Execute Club Locker Lambda")
            assertThat(received[0].message).contains("ExceptionInInitializerError")
            assertThat(received[0].message).contains("key123")
            assertThat(received[0].message).contains("val456")
        }

    @Test
    fun `notifier sends a reasonable message on success monitoring slots`() =
        runTest {
            val slot = Slot(1, 1, 1411, 1, 1, Instant.parse("2019-06-01T00:31:31Z").epochSecond)
            notifier.publishFoundOpenSlot(listOf(slot))

            assertThat(received).hasSize(1)
            assertThat(received[0].topicArn).isEqualTo("some-arn")
            assertThat(received[0].subject).contains("Found new open slots on Club Locker")
            assertThat(received[0].message).contains("Friday, May 31: Court 1, 0:01 am-0:01 am")
        }
}

private data class PublishedMessage(
    val topicArn: String,
    val subject: String,
    val message: String
)
