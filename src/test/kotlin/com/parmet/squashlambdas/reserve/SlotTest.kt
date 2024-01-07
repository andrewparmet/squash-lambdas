package com.parmet.squashlambdas.reserve

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalTime

class SlotTest {
    @Test
    fun `start time is output correctly`() {
        assertThat(
            Slot(
                LocalTime.parse("06:30"),
                LocalTime.parse("06:30"),
            ).startTime,
        ).isEqualTo(630)
    }

    @Test
    fun `end time is output correctly`() {
        assertThat(
            Slot(
                LocalTime.parse("06:30"),
                LocalTime.parse("16:30"),
            ).endTime,
        ).isEqualTo(1630)
    }

    @Test
    fun `slot is output correctly`() {
        assertThat(
            Slot(
                LocalTime.parse("17:15"),
                LocalTime.parse("18:00"),
            ).slot,
        ).isEqualTo("17:15-18:00")
    }
}
