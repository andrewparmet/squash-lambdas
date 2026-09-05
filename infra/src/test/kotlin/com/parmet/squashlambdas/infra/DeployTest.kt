package com.parmet.squashlambdas.infra

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class DeployTest {
    @Test
    fun `retain aliased and newest Lambda versions`() {
        assertThat(versionsToDelete((1..10).toList(), setOf(2), 5)).containsExactly(1, 3, 4, 5).inOrder()
    }
}
