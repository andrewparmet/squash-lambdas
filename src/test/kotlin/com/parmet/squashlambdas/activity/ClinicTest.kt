package com.parmet.squashlambdas.activity

import com.google.gson.Gson
import org.junit.Test
import java.time.Instant

class ClinicTest {
    private val gson = Gson()

    @Test
    fun `match can be serialized`() {
        gson.toJson(Clinic(Court.Court2, Instant.now(), Instant.now(), "foo"))
    }
}
