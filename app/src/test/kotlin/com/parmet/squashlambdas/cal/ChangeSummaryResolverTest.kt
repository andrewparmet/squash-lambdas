package com.parmet.squashlambdas.cal

import com.google.common.truth.Truth.assertThat
import com.parmet.squashlambdas.activity.Clinic
import com.parmet.squashlambdas.activity.Court
import com.parmet.squashlambdas.activity.Match
import com.parmet.squashlambdas.activity.Player
import com.parmet.squashlambdas.clublocker.ClubLockerClient
import com.parmet.squashlambdas.clublocker.Reservation
import com.parmet.squashlambdas.clublocker.ReservationPlayer
import com.parmet.squashlambdas.clublocker.Slot
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

private val start = Instant.parse("2026-09-08T21:00:00Z")
private val end = Instant.parse("2026-09-08T22:00:00Z")

class ChangeSummaryResolverTest {
    private val client = mockk<ClubLockerClient>()
    private val resolver = ClubLockerChangeSummaryResolver(client)

    @Test
    fun `populate players from current reservation`() =
        runTest {
            coEvery { client.slotsTaken(LocalDate.parse("2026-09-08"), LocalDate.parse("2026-09-08")) } returns
                listOf(Slot(1, 2, 1690, 1700, 1800, start.epochSecond, "match"))
            coEvery { client.reservation(2) } returns
                Reservation(
                    listOf(
                        ReservationPlayer("Host Player", true),
                        ReservationPlayer("First Player", false),
                        ReservationPlayer("Second Player", false),
                        ReservationPlayer("Third Player", false),
                    )
                )

            val resolved = resolver.resolve(ChangeSummary(Action.Update, match()))

            assertThat(resolved.action).isEqualTo(Action.Update)
            assertThat((resolved.activity as Match).players)
                .containsExactly(
                    Player(name = "First Player"),
                    Player(name = "Second Player"),
                    Player(name = "Third Player"),
                )
        }

    @Test
    fun `delete when current slot is unavailable`() =
        runTest {
            coEvery { client.slotsTaken(any(), any()) } returns emptyList()
            val change = ChangeSummary(Action.Update, match())

            assertThat(resolver.resolve(change)).isEqualTo(change.copy(action = Action.Delete))
        }

    @Test
    fun `replace a stale deletion with the current reservation`() =
        runTest {
            coEvery { client.slotsTaken(any(), any()) } returns
                listOf(Slot(1, 2, 1690, 1700, 1800, start.epochSecond, "match"))
            coEvery { client.reservation(2) } returns
                Reservation(
                    listOf(
                        ReservationPlayer("Host Player", true),
                        ReservationPlayer("Current Player", false),
                    )
                )

            val resolved = resolver.resolve(ChangeSummary(Action.Delete, match()))

            assertThat(resolved).isEqualTo(
                ChangeSummary(Action.Update, match().copy(players = setOf(Player(name = "Current Player"))))
            )
        }

    @Test
    fun `confirm a clinic against current Club Locker slots`() =
        runTest {
            coEvery { client.slotsTaken(any(), any()) } returns
                listOf(Slot(1, 2, 1689, 1700, 1800, start.epochSecond, "lesson"))
            val clinic = clinic()

            assertThat(resolver.resolve(ChangeSummary(Action.Create, clinic)))
                .isEqualTo(ChangeSummary(Action.Update, clinic))
        }

    @Test
    fun `delete a clinic absent from current Club Locker slots`() =
        runTest {
            coEvery { client.slotsTaken(any(), any()) } returns emptyList()
            val clinic = clinic()

            assertThat(resolver.resolve(ChangeSummary(Action.Create, clinic)))
                .isEqualTo(ChangeSummary(Action.Delete, clinic))
        }

    @Test
    fun `delete a clinic when the current slot is a match`() =
        runTest {
            coEvery { client.slotsTaken(any(), any()) } returns
                listOf(Slot(1, 2, 1689, 1700, 1800, start.epochSecond, "match"))
            val clinic = clinic()

            assertThat(resolver.resolve(ChangeSummary(Action.Create, clinic)))
                .isEqualTo(ChangeSummary(Action.Delete, clinic))
        }
}

private fun match() =
    Match(
        court = Court.TennisCourt,
        start = start,
        end = end,
        origin = "email",
        players = setOf(Player(name = "Parsed Player")),
    )

private fun clinic() =
    Clinic(
        court = Court.Court3,
        start = start,
        end = end,
        origin = "email"
    )
