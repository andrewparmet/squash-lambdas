package com.parmet.squashlambdas.monitor

import com.google.common.truth.Truth.assertThat
import com.parmet.squashlambdas.clublocker.ClubLockerClient
import com.parmet.squashlambdas.clublocker.Slot
import com.parmet.squashlambdas.clublocker.TokenStatusManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.LocalDate

private val date = LocalDate.of(2026, 9, 5)

class SlotsTrackerTest {
    private val client = mockk<ClubLockerClient>()
    private val storage = mockk<SlotStorageManager>()
    private val tokenStatusManager = mockk<TokenStatusManager>(relaxed = true)
    private val tracker = SlotsTracker(client, storage, tokenStatusManager)

    @Test
    fun `rebooked slot is not newly open`() =
        runTest {
            val previous = slot(id = 1, reservationId = 10)
            val current = slot(id = 2, reservationId = 20)
            coEvery { storage.loadLatest(date) } returns listOf(previous)
            coEvery { client.slotsTaken(date, date) } returns listOf(current)
            coEvery { storage.save(date, listOf(current)) } returns Unit

            assertThat(tracker.findNewlyOpen(date)).isEmpty()
            coVerify(exactly = 1) { storage.save(date, listOf(current)) }
        }

    @Test
    fun `removed slot is newly open`() =
        runTest {
            val previous = slot(id = 1, reservationId = 10)
            coEvery { storage.loadLatest(date) } returns listOf(previous)
            coEvery { client.slotsTaken(date, date) } returns emptyList()
            coEvery { storage.save(date, emptyList()) } returns Unit

            assertThat(tracker.findNewlyOpen(date)).containsExactly(previous)
        }
}

private fun slot(id: Int, reservationId: Int) =
    Slot(
        id = id,
        reservationId = reservationId,
        court = 1,
        startTime = 1_800,
        endTime = 1_845,
        startUtc = 1_778_000_400,
        type = "match",
    )
