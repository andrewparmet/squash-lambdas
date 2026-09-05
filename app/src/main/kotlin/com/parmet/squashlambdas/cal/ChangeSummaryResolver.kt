package com.parmet.squashlambdas.cal

import com.parmet.squashlambdas.activity.Match
import com.parmet.squashlambdas.activity.Player
import com.parmet.squashlambdas.clublocker.COURTS_BY_ID
import com.parmet.squashlambdas.clublocker.ClubLockerClient
import com.parmet.squashlambdas.util.inBoston
import dev.zacsweers.metro.Inject

fun interface ChangeSummaryResolver {
    suspend fun resolve(change: ChangeSummary): ChangeSummary
}

@Inject
class ClubLockerChangeSummaryResolver(
    private val client: ClubLockerClient
) : ChangeSummaryResolver {
    override suspend fun resolve(change: ChangeSummary): ChangeSummary {
        val match = change.activity as? Match ?: return change
        if (change.action == Action.None) {
            return change
        }
        val date = match.start.inBoston().toLocalDate()
        val courtId = COURTS_BY_ID.inverse().getValue(match.court)
        val slot =
            client.slotsTaken(date, date).singleOrNull { it.court == courtId && it.startUtc == match.start.epochSecond }
                ?: return change.copy(action = Action.Delete)
        val players =
            client.reservation(slot.reservationId).players.filterNot { it.isMyself }.map { player ->
                Player(name = player.text)
            }.toSet()
        return change.copy(action = Action.Update, activity = match.copy(players = players))
    }
}
