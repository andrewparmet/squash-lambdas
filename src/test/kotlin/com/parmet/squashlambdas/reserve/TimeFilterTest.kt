package com.parmet.squashlambdas.reserve

import com.google.common.truth.Truth.assertThat
import org.joda.time.DateTime
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class TimeFilterTest {
    @Test
    fun `makes a reservation at 1201 am local EST`() {
        val time = DateTime.parse("2019-02-05T05:01:00Z")
        assertThat(
            TimeFilter(time, time.toFixedClock())
                .filterBasedOnBostonTime()
                .reason
        ).isNull()
    }

    @Test
    fun `does not make a reservation at 1101 pm local EST`() {
        val time = DateTime.parse("2019-02-05T04:01:00Z")
        assertThat(
            TimeFilter(time, time.toFixedClock())
                .filterBasedOnBostonTime()
                .reason
        ).isEqualTo(
            "Date 2019-02-12 is too far in the future to reserve now (2019-02-04T23:01-05:00[America/New_York])"
        )
    }

    @Test
    fun `makes a reservation at 1201 am local EDT`() {
        val time = DateTime.parse("2019-04-05T04:01:00Z")
        assertThat(
            TimeFilter(time, time.toFixedClock())
                .filterBasedOnBostonTime()
                .reason
        ).isNull()
    }

    @Test
    fun `does not make a reservation at 0101 am local EDT`() {
        val time = DateTime.parse("2019-04-05T05:01:00Z")
        assertThat(
            TimeFilter(time, time.toFixedClock())
                .filterBasedOnBostonTime()
                .reason
        ).isEqualTo(
            "Not scheduling past 1 am; " +
                "assuming that the 12 am run already ran (2019-04-05T01:01-04:00[America/New_York])"
        )
    }

    private fun DateTime.toFixedClock() =
        Clock.fixed(Instant.ofEpochMilli(millis), ZoneOffset.UTC)
}
