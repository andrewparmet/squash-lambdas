package com.parmet.squashlambdas.monitor

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class TimeFormatterTest {
    @Test
    fun `test AM time`() {
        assertThat(TimeFormatter.formatTime(530)).isEqualTo("5:30 am")
    }

    @Test
    fun `test PM time`() {
        assertThat(TimeFormatter.formatTime(1730)).isEqualTo("5:30 pm")
    }

    @Test
    fun `test less than ten minutes time`() {
        assertThat(TimeFormatter.formatTime(1700)).isEqualTo("5:00 pm")
    }
}
