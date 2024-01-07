package com.parmet.squashlambdas.reserve

import com.google.common.truth.Truth.assertThat
import com.parmet.squashlambdas.testutil.getResourceAsString
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ScheduleTest {
    @Test
    fun `schedule is parsed and interpreted correctly`() {
        val schedule = Schedule.fromString(getResourceAsString("schedule.txt"))

        assertThat(schedule.shouldMakeReservation(LocalDate.parse("2019-02-04"))).isTrue()
        assertThat(schedule.shouldMakeReservation(LocalDate.parse("2019-02-05"))).isTrue()
        assertThat(schedule.shouldMakeReservation(LocalDate.parse("2019-02-06"))).isTrue()
        assertThat(schedule.shouldMakeReservation(LocalDate.parse("2019-02-07"))).isFalse()
        assertThat(schedule.shouldMakeReservation(LocalDate.parse("2019-02-08"))).isFalse()
        assertThat(schedule.shouldMakeReservation(LocalDate.parse("2019-02-09"))).isFalse()
        assertThat(schedule.shouldMakeReservation(LocalDate.parse("2019-02-10"))).isFalse()
    }
}
