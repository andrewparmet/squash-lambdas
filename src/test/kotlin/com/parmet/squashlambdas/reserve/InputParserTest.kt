package com.parmet.squashlambdas.reserve

import com.google.common.truth.Truth.assertThat
import com.parmet.squashlambdas.testutil.PARSER
import com.parmet.squashlambdas.testutil.getResourceAsString
import org.junit.Test
import java.time.LocalDate

class InputParserTest {
    @Test
    fun `input is parsed correctly`() {
        assertThat(
            InputParser.parseRequestDate(
                PARSER.parse(getResourceAsString("reservation-trigger.json"))
            )
        ).isEqualTo(LocalDate.of(2017, 1, 6))
    }
}
