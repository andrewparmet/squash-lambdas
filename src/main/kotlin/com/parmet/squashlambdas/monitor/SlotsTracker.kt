package com.parmet.squashlambdas.monitor

import com.parmet.squashlambdas.clublocker.ClubLockerClient
import com.parmet.squashlambdas.clublocker.Slot
import java.time.LocalDate

internal class SlotsTracker(
    private val client: ClubLockerClient,
    private val slotStorageManager: SlotStorageManager
) {
    fun findNewlyOpen(date: LocalDate): List<Slot> {
        val lastSlotsTaken = slotStorageManager.loadLatest(date)

        val slotsTaken = client.slotsTaken(date, date)

        slotStorageManager.save(date, slotsTaken)

        return lastSlotsTaken - slotsTaken
    }
}
