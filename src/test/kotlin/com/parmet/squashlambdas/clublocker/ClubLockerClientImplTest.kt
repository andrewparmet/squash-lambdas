package com.parmet.squashlambdas.clublocker

import com.google.common.truth.Truth.assertThat
import com.parmet.squashlambdas.activity.Court
import com.parmet.squashlambdas.activity.Match
import com.parmet.squashlambdas.activity.Player
import com.parmet.squashlambdas.testutil.ConfiguredTest
import org.junit.Before
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.util.concurrent.TimeUnit

@Disabled
class ClubLockerClientImplTest : ConfiguredTest() {
    @Before
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
        assertThat(User(0, "Parmet, Andrew").fullName)
            .isEqualTo("Andrew Parmet")
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
        val resp =
            client.makeReservation(
                Match(
                    Court.Court6,
                    Instant.parse("2019-02-04T23:00:00Z"),
                    Instant.parse("2019-02-04T23:45:00Z"),
                    "",
                    setOf(Player(email = email))
                )
            )

        println(resp)
        assertThat(resp).isInstanceOf(ReservationResp.Success::class.java)
    }
}
