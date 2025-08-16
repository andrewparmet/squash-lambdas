package com.parmet.squashlambdas.activity

import com.parmet.squashlambdas.json.Json
import org.junit.jupiter.api.Test
import java.time.Instant

class ClinicTest {
    @Test
    fun `match can be serialized`() {
        Json.mapper.writeValueAsString(Clinic(Court.Court2, Instant.now(), Instant.now(), "foo"))
    }
}
