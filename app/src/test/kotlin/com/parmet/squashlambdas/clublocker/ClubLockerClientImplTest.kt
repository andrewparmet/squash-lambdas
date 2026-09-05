package com.parmet.squashlambdas.clublocker

import com.google.common.truth.Truth.assertThat
import com.parmet.squashlambdas.activity.Court
import com.parmet.squashlambdas.activity.Match
import com.parmet.squashlambdas.activity.Player
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

@Disabled
class ClubLockerClientImplTest {
    private val email = "email"
    private val tokenManager = mockk<TokenManager> { coEvery { getToken() } returns "test-token" }
    private val client = ClubLockerClientImpl(tokenManager)

    @BeforeEach
    fun startClient() {
        client.init()
    }

    @Test
    fun `test user info`() =
        runTest { println(client.user()) }

    @Test
    fun `test courts info`() =
        runTest { println(client.courts()) }

    @Test
    fun `test directory info`() =
        runTest { println(client.directory()) }

    @Test
    fun `test directory name reversal`() {
        assertThat(User(0, "Last, First").fullName).isEqualTo("First Last")
    }

    @Test
    fun `test taken slots info`() =
        runTest { println(client.slotsTaken(LocalDate.now(), LocalDate.now())) }

    @Test
    fun `test make reservation failure`() =
        runTest {
            val match =
                Match(
                    Court.Court7,
                    Instant.parse("2018-02-03T23:00:00Z"),
                    Instant.parse("2018-02-03T23:44:00Z"),
                    "",
                    setOf(Player(email = email)),
                )

            assertThat(
                client.makeReservation(match)
            ).isEqualTo(ReservationResp.Error(500, "Court doesn't have that slot", match))
        }

    @Test
    fun `test make reservation success`() =
        runTest {
            val start = Instant.parse("2025-08-18T19:00:00Z")
            val resp =
                client.makeReservation(
                    Match(
                        Court.TennisCourt,
                        start,
                        start + Duration.ofMinutes(60),
                        "",
                        setOf(Player(name = "First Last", email = email)),
                    )
                )

            println(resp)
            if (resp is ReservationResp.Failure) {
                resp.t.printStackTrace()
            }
            assertThat(resp).isInstanceOf(ReservationResp.Success::class.java)
        }
}
