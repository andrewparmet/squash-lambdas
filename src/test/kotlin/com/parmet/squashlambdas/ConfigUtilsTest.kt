package com.parmet.squashlambdas

import com.google.common.truth.Truth.assertThat
import com.parmet.squashlambdas.activity.Court.Court1
import com.parmet.squashlambdas.activity.Court.Court2
import com.parmet.squashlambdas.activity.Court.Court3
import org.junit.jupiter.api.Test
import java.time.LocalTime

class ConfigUtilsTest {
    @Test
    fun `parsing courts works`() {
        assertThat(
            getPreferredCourts(
                """
                    Court 1
                    Court 2
                    Court 3
                """.trimIndent(),
            )
        ).containsExactly(Court1, Court2, Court3).inOrder()
    }

    @Test
    fun `parsing times works`() {
        assertThat(
            getPreferredTimes(
                """
                    18:00
                    18:45
                    19:30
                """.trimIndent(),
            )
        ).containsExactly(
            LocalTime.of(18, 0),
            LocalTime.of(18, 45),
            LocalTime.of(19, 30)
        ).inOrder()
    }
}
