package com.parmet.squashlambdas.activity

import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.EventDateTime
import com.google.common.truth.Truth.assertThat
import com.parmet.squashlambdas.json.Json
import org.junit.jupiter.api.Test
import java.time.Instant

class MatchTest {
    @Test
    fun `match can be serialized`() {
        Json.mapper.writeValueAsString(
            Match(
                Court.Court2,
                Instant.now(),
                Instant.now(),
                "",
                setOf(Player(name = "Me"))
            )
        )
    }

    @Test
    fun `toEvent() form is correct`() {
        val event =
            Match(
                Court.Court2,
                Instant.parse("2018-03-26T22:45:00Z"),
                Instant.parse("2018-03-26T23:30:00Z"),
                "foobar",
                setOf(Player(name = "Philipp Rimmler"))
            ).toEvent()

        assertThat(event.start)
            .isEqualTo(EventDateTime().setDateTime(DateTime(Instant.parse("2018-03-26T22:45:00Z").toEpochMilli())))

        assertThat(event.end)
            .isEqualTo(EventDateTime().setDateTime(DateTime(Instant.parse("2018-03-26T23:30:00Z").toEpochMilli())))

        assertThat(event.location)
            .isEqualTo("Court 2, Tennis and Racquet Club")

        assertThat(event.summary)
            .isEqualTo("Squash v. Philipp Rimmler")

        assertThat(event.description)
            .isEqualTo(
                "Match(court=Court 2, start=2018-03-26T22:45:00Z, end=2018-03-26T23:30:00Z, " +
                    "origin=foobar, players=[Player(name=Philipp Rimmler, email=null, memberId=null)])"
            )
    }
}
