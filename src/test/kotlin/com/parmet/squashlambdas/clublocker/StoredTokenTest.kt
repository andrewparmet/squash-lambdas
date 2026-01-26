package com.parmet.squashlambdas.clublocker

import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.truth.Truth.assertThat
import com.parmet.squashlambdas.json.Json
import org.junit.jupiter.api.Test
import java.time.Instant

class StoredTokenTest {
    @Test
    fun `create sets current time`() {
        val before = Instant.now()
        val token = StoredToken.create("test-token")
        val after = Instant.now()

        assertThat(token.token).isEqualTo("test-token")
        assertThat(token.updateTime).isAtLeast(before)
        assertThat(token.updateTime).isAtMost(after)
    }

    @Test
    fun `serializes to JSON`() {
        val token = StoredToken("my-token", Instant.parse("2026-01-25T12:00:00Z"))
        val json = Json.mapper.writeValueAsString(token)

        assertThat(json).contains("\"token\":\"my-token\"")
        assertThat(json).contains("\"updateTime\"")
    }

    @Test
    fun `deserializes from JSON`() {
        val json = """{"token":"my-token","updateTime":"2026-01-25T12:00:00Z"}"""
        val token: StoredToken = Json.mapper.readValue(json)

        assertThat(token.token).isEqualTo("my-token")
        assertThat(token.updateTime).isEqualTo(Instant.parse("2026-01-25T12:00:00Z"))
    }

    @Test
    fun `round trip serialization`() {
        val original = StoredToken.create("round-trip-token")
        val json = Json.mapper.writeValueAsString(original)
        val deserialized: StoredToken = Json.mapper.readValue(json)

        assertThat(deserialized).isEqualTo(original)
    }
}
