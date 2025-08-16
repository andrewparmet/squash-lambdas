package com.parmet.squashlambdas.activity

import com.google.common.truth.Truth.assertThat
import com.parmet.squashlambdas.cal.emailFromBody
import org.junit.jupiter.api.Test
import java.time.Instant

class TimeParserTest {
    @Test
    fun `email can be parsed into start and end times`() {
        val body = emailFromBody("reservationCreated2").body

        assertThat(TimeParser.parse(body).start)
            .isEqualTo(Instant.parse("2018-03-29T01:00:00Z"))
        assertThat(TimeParser.parse(body).end)
            .isEqualTo(Instant.parse("2018-03-29T01:45:00Z"))
    }
}
