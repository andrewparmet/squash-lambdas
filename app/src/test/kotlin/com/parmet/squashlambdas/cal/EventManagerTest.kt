package com.parmet.squashlambdas.cal

import com.parmet.squashlambdas.activity.Court
import com.parmet.squashlambdas.activity.Match
import com.parmet.squashlambdas.activity.Player
import com.parmet.squashlambdas.testutil.ConfiguredTest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.Instant

@Disabled
class EventManagerTest : ConfiguredTest() {
    @Test
    fun `create creates a match`() =
        runTest {
            manager().create(
                Match(
                    Court.Court3,
                    Instant.parse("2018-06-25T23:30:00Z"),
                    Instant.parse("2018-06-26T00:15:00Z"),
                    "",
                    setOf()
                )
            )
        }

    @Test
    fun `update updates a match`() =
        runTest {
            manager().update(
                Match(
                    Court.Court3,
                    Instant.parse("2018-06-25T23:30:00Z"),
                    Instant.parse("2018-06-26T00:15:00Z"),
                    "",
                    setOf(Player(name = "Logan Ramseyer")),
                )
            )
        }

    @Test
    fun `delete deletes a match`() =
        runTest {
            manager().delete(
                Match(
                    Court.Court3,
                    Instant.parse("2018-06-25T23:30:00Z"),
                    Instant.parse("2018-06-26T00:15:00Z"),
                    "",
                    setOf(Player(name = "Logan Ramseyer")),
                )
            )
        }

    private fun manager() =
        EventManager(
            object : CalendarProvider {
                override suspend fun get() =
                    calendar
            },
            emailNotificationConfig.googleCal
        )
}
