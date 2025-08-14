package com.parmet.squashlambdas.clublocker

import com.google.common.truth.Truth.assertThat
import com.parmet.squashlambdas.activity.Court
import com.parmet.squashlambdas.activity.Match
import com.parmet.squashlambdas.activity.Player
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.concurrent.TimeUnit

@Disabled
class ClubLockerClientImplTest {
    private val email = "email"
    private val client = ClubLockerClientImpl(ClubLockerUser(email, "password"))

    @BeforeEach
    fun startClient() {
        client.startAsync().awaitRunning(3, TimeUnit.SECONDS)
    }

    @Test
    fun `test user info`() {
        println(client.user())
    }

    @Test
    fun `test courts info`() {
        println(client.courts())
    }

    @Test
    fun `test directory info`() {
        println(client.directory())
    }

    @Test
    fun `test directory name reversal`() {
        assertThat(User(0, "Last, First").fullName)
            .isEqualTo("First Last")
    }

    @Test
    fun `test taken slots info`() {
        println(client.slotsTaken(LocalDate.now(), LocalDate.now()))
    }

    @Test
    fun `test make reservation failure`() {
        val match =
            Match(
                Court.Court7,
                // Some legal date
                Instant.parse("2018-02-03T23:00:00Z"),
                // Not 45 minutes
                Instant.parse("2018-02-03T23:44:00Z"),
                "",
                setOf(Player(email = email))
            )

        val resp = client.makeReservation(match)

        assertThat(resp).isEqualTo(
            ReservationResp.Error(500, "Court doesn't have that slot", match)
        )
    }

    @Test
    fun `test make reservation success`() {
        val start = Instant.parse("2025-08-18T19:00:00Z")
        val resp =
            client.makeReservation(
                Match(
                    Court.TennisCourt,
                    start,
                    start + Duration.ofMinutes(60),
                    "",
                    setOf(Player(name = "First Last", email = email))
                )
            )

        println(resp)
        if (resp is ReservationResp.Failure) {
            resp.t.printStackTrace()
        }
        assertThat(resp).isInstanceOf(ReservationResp.Success::class.java)
    }
}
