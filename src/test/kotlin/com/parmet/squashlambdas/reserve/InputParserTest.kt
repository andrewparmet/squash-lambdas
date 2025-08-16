package com.parmet.squashlambdas.reserve

import com.google.common.truth.Truth.assertThat
import com.parmet.squashlambdas.json.Json
import com.parmet.squashlambdas.testutil.getResourceAsString
import org.junit.jupiter.api.Test
import java.time.LocalDate

class InputParserTest {
    @Test
    fun `input is parsed correctly`() {
        assertThat(
            InputParser.parseRequestDate(
                Json.mapper.readTree(getResourceAsString("reservation-trigger.json"))
            )
        ).isEqualTo(LocalDate.of(2017, 1, 6))
    }
}
