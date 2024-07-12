package com.parmet.squashlambdas.activity

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class CourtTest {
    @Test
    fun `fromLocationString can parse squash courts`() {
        assertThat(Court.fromLocationString("Court #1 - Squash"))
            .isEqualTo(Court.Court1)

        assertThat(Court.fromLocationString("Court #2 - Squash"))
            .isEqualTo(Court.Court2)

        assertThat(Court.fromLocationString("Court #3 - Squash"))
            .isEqualTo(Court.Court3)
    }

    @Test
    fun `fromLocationString can parse hardball courts`() {
        assertThat(Court.fromLocationString("Court #5 - Hardball"))
            .isEqualTo(Court.Court5)

        assertThat(Court.fromLocationString("Court #6 - Hardball"))
            .isEqualTo(Court.Court6)

        assertThat(Court.fromLocationString("Court #7 - Hardball"))
            .isEqualTo(Court.Court7)
    }
}
