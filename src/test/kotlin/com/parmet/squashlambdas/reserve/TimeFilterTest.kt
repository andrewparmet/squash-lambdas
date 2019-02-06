package com.parmet.squashlambdas.reserve

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class TimeFilterTest {
    @Test
    fun `makes a reservation at 1201 am local EST`() {
        val time = Instant.parse("2019-02-05T05:01:00Z")
        val clock = Clock.fixed(time, ZoneOffset.UTC)
        assertThat(
            TimeFilter(InputParser.parseRequestDate(input(time)), clock)
                .filterBasedOnBostonTime()
                .reason
        ).isNull()
    }

    @Test
    fun `does not make a reservation at 1101 pm local EST`() {
        val time = Instant.parse("2019-02-05T04:01:00Z")
        val clock = Clock.fixed(time, ZoneOffset.UTC)
        assertThat(
            TimeFilter(InputParser.parseRequestDate(input(time)), clock)
                .filterBasedOnBostonTime()
                .reason
        ).isEqualTo(
            "Date 2019-02-12 is too far in the future to reserve now (2019-02-04T23:01-05:00[America/New_York])"
        )
    }

    @Test
    fun `makes a reservation at 1201 am local EDT`() {
        val time = Instant.parse("2019-04-05T04:01:00Z")
        val clock = Clock.fixed(time, ZoneOffset.UTC)
        assertThat(
            TimeFilter(InputParser.parseRequestDate(input(time)), clock)
                .filterBasedOnBostonTime()
                .reason
        ).isNull()
    }

    @Test
    fun `does not make a reservation at 0101 am local EDT`() {
        val time = Instant.parse("2019-04-05T05:01:00Z")
        val clock = Clock.fixed(time, ZoneOffset.UTC)
        assertThat(
            TimeFilter(InputParser.parseRequestDate(input(time)), clock)
                .filterBasedOnBostonTime()
                .reason
        ).isEqualTo(
            "Not scheduling past 1 am; " +
                "assuming that the 12 am run already ran (2019-04-05T01:01-04:00[America/New_York])"
        )
    }

    private fun input(instant: Instant) =
        InputParser.Input("", UUID.randomUUID(), "", "", "", instant, "", listOf(), mapOf())
}
