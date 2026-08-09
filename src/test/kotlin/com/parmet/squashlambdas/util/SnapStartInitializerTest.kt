package com.parmet.squashlambdas.util

import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.crac.Context
import org.crac.Resource
import org.junit.jupiter.api.Test

class SnapStartInitializerTest {
    @Test
    fun `initializes once before checkpoint`() {
        var initializationCount = 0
        val initializer = SnapStartInitializer { initializationCount++ }

        initializer.beforeCheckpoint(mockk<Context<out Resource>>())
        initializer.initialize()

        assertThat(initializationCount).isEqualTo(1)
    }
}
