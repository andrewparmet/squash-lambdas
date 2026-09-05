package com.parmet.squashlambdas.monitor

import com.parmet.squashlambdas.clublocker.Slot
import com.parmet.squashlambdas.json.Json
import com.parmet.squashlambdas.util.BOSTON
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Instant
import java.time.LocalDate

@Inject
class SlotStorageManager(
    private val table: SlotSnapshotTable
) {
    private val logger = KotlinLogging.logger { }

    suspend fun save(date: LocalDate, slots: List<Slot>) {
        table.put(
            SlotSnapshot(
                filename = "$date/taken",
                entries = slots.map { Json.encode(it) },
                modifiedTime = Instant.now().toString(),
                ttl = date.plusDays(1).atStartOfDay(BOSTON).toEpochSecond(),
            )
        )

        logger.info { "Saved latest slots taken for $date: $slots" }
    }

    suspend fun loadLatest(date: LocalDate): List<Slot> {
        val snapshot = table.get("$date/taken")
        return if (snapshot == null) {
            emptyList()
        } else {
            snapshot.entries.map { json -> Json.decode<Slot>(json) }
                .also { logger.info { "Loaded latest snapshot of taken slots: $it" } }
        }
    }
}
