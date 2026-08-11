package com.parmet.squashlambdas.clublocker

import com.google.common.truth.Truth.assertThat
import com.parmet.squashlambdas.activity.Court
import com.parmet.squashlambdas.activity.Match
import com.parmet.squashlambdas.activity.Player
import org.junit.jupiter.api.Test
import java.time.Instant

class ReservationResponseTest {
    private val match =
        Match(
            court = Court.TennisCourt,
            start = Instant.parse("2026-08-18T22:00:00Z"),
            end = Instant.parse("2026-08-18T23:00:00Z"),
            origin = "test",
            players = setOf(Player(name = "Andrew Parmet"))
        )

    @Test
    fun `parses primitive error`() {
        val response = parseReservationResponse(400, """{"error":"Court doesn't have that slot"}""", match)

        assertThat(response).isEqualTo(ReservationResp.Error(400, "Court doesn't have that slot", match))
    }

    @Test
    fun `extracts message from structured error`() {
        val response =
            parseReservationResponse(
                500,
                """{"error":{"message":"Slot cannot be booked because there is an overlapping match at times 18:00-19:00"}}""",
                match
            )

        assertThat(response)
            .isEqualTo(
                ReservationResp.Error(
                    500,
                    "Slot cannot be booked because there is an overlapping match at times 18:00-19:00",
                    match
                )
            )
    }
}
