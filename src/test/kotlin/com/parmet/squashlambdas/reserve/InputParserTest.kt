package com.parmet.squashlambdas.reserve

import com.google.common.truth.Truth.assertThat
import com.google.gson.JsonParser
import com.parmet.squashlambdas.testutil.getResourceAsString
import org.junit.Test
import java.time.LocalDate

class InputParserTest {
    @Test
    fun `input is parsed correctly`() {
        assertThat(
            InputParser.parseRequestDate(
                JsonParser.parseString(getResourceAsString("reservation-trigger.json"))
            )
        ).isEqualTo(LocalDate.of(2017, 1, 6))
    }
}
