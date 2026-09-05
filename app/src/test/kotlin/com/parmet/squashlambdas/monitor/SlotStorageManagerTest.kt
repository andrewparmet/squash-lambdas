package com.parmet.squashlambdas.monitor

import com.google.common.truth.Truth.assertThat
import com.parmet.squashlambdas.clublocker.Slot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.LocalDate

class SlotStorageManagerTest {
    private val table = mockk<SlotSnapshotTable>(relaxed = true)
    private val manager = SlotStorageManager(table)

    @Test
    fun `saved slots expire after their date`() =
        runTest {
            val snapshot = slot<SlotSnapshot>()
            manager.save(LocalDate.of(2026, 9, 5), listOf(Slot(1, 2, 3, 1800, 1845, 1788645600, "match")))

            coVerify { table.put(capture(snapshot)) }
            assertThat(snapshot.captured.ttl).isEqualTo(1788667200)
        }

    @Test
    fun `saving no slots uses an empty list`() =
        runTest {
            val snapshot = slot<SlotSnapshot>()
            manager.save(LocalDate.of(2026, 9, 5), emptyList())

            coVerify { table.put(capture(snapshot)) }
            assertThat(snapshot.captured.entries).isEmpty()
        }

    @Test
    fun `missing snapshot loads as no slots`() =
        runTest {
            coEvery { table.get(any()) } returns null
            assertThat(manager.loadLatest(LocalDate.of(2026, 9, 5))).isEmpty()
        }

    @Test
    fun `saved slots round trip`() =
        runTest {
            val date = LocalDate.of(2026, 9, 5)
            val slots = listOf(Slot(1, 2, 3, 1800, 1845, 1788645600, "match"))
            val snapshot = slot<SlotSnapshot>()
            manager.save(date, slots)
            coVerify { table.put(capture(snapshot)) }
            coEvery { table.get(any()) } returns snapshot.captured

            assertThat(manager.loadLatest(date)).containsExactlyElementsIn(slots)
        }
}
