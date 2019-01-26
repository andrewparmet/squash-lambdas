package com.parmet.squashlambdas.cal

import com.google.common.collect.ImmutableSet
import com.parmet.squashlambdas.activity.Court
import com.parmet.squashlambdas.activity.Match
import com.parmet.squashlambdas.testutil.ConfiguredTest
import java.time.Instant
import org.junit.Ignore
import org.junit.Test

@Ignore // These actually modify a calendar
class EventManagerTest : ConfiguredTest() {
    @Test
    fun `create creates a match`() {
        manager().create(
            Match(
                Court.Court3,
                Instant.parse("2018-06-25T23:30:00Z"),
                Instant.parse("2018-06-26T00:15:00Z"),
                setOf()))
    }

    @Test
    fun `update updates a match`() {
        manager().update(
            Match(
                Court.Court3,
                Instant.parse("2018-06-25T23:30:00Z"),
                Instant.parse("2018-06-26T00:15:00Z"),
                setOf("Logan Ramseyer")))
    }

    @Test
    fun `delete deletes a match`() {
        manager().delete(
            Match(
                Court.Court3,
                Instant.parse("2018-06-25T23:30:00Z"),
                Instant.parse("2018-06-26T00:15:00Z"),
                setOf("Logan Ramseyer")))
    }

    private fun manager() = EventManager(calendar(), config().getString("google.calendarId"))
}
