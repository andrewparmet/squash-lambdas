package com.parmet.squashlambdas.monitor

import com.parmet.squashlambdas.clublocker.ClubLockerClient
import com.parmet.squashlambdas.clublocker.Slot
import com.parmet.squashlambdas.clublocker.TokenStatusManager
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.http.client.HttpResponseException
import java.time.LocalDate
import javax.inject.Inject

private val logger = KotlinLogging.logger { }

class SlotsTracker @Inject constructor(
    private val client: ClubLockerClient,
    private val slotStorageManager: SlotStorageManager,
    private val tokenStatusManager: TokenStatusManager
) {
    fun findNewlyOpen(date: LocalDate): List<Slot> {
        val lastSlotsTaken = slotStorageManager.loadLatest(date)

        val slotsTaken =
            try {
                client.slotsTaken(date, date)
            } catch (e: HttpResponseException) {
                if (e.statusCode in listOf(401, 403)) {
                    logger.warn { "Auth failure (${e.statusCode}), marking token invalid" }
                    tokenStatusManager.markTokenInvalid("HTTP ${e.statusCode}: ${e.reasonPhrase}")
                }
                throw e
            }

        slotStorageManager.save(date, slotsTaken)

        return lastSlotsTaken - slotsTaken
    }
}
