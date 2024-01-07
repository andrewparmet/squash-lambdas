package com.parmet.squashlambdas.activity

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class CourtTest {
    @Test
    fun `fromLocationString can parse squash courts`() {
        assertThat(Court.fromLocationString("Court: Court #1 /-"))
            .isEqualTo(Court.Court1)

        assertThat(Court.fromLocationString("Court: Court #2 /-"))
            .isEqualTo(Court.Court2)

        assertThat(Court.fromLocationString("Court: Court #3 /-"))
            .isEqualTo(Court.Court3)
    }

    @Test
    fun `fromLocationString can parse hardball courts`() {
        assertThat(Court.fromLocationString("Court: Court #5 /-"))
            .isEqualTo(Court.Court5)

        assertThat(Court.fromLocationString("Court: Court #6 /-"))
            .isEqualTo(Court.Court6)

        assertThat(Court.fromLocationString("Court: Court #7 /-"))
            .isEqualTo(Court.Court7)
    }
}
