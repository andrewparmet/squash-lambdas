package com.parmet.squashlambdas.cal

import com.parmet.squashlambdas.activity.AbstractActivity
import com.parmet.squashlambdas.activity.Clinic
import com.parmet.squashlambdas.activity.Match
import com.parmet.squashlambdas.activity.Player
import com.parmet.squashlambdas.clublocker.COURTS_BY_ID
import com.parmet.squashlambdas.clublocker.ClubLockerClient
import com.parmet.squashlambdas.util.inBoston
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

fun interface ChangeSummaryResolver {
    suspend fun resolve(change: ChangeSummary): ChangeSummary
}

@ContributesBinding(AppScope::class)
@Inject
class ClubLockerChangeSummaryResolver(
    private val client: ClubLockerClient
) : ChangeSummaryResolver {
    override suspend fun resolve(change: ChangeSummary): ChangeSummary {
        if (change.action == Action.None) {
            return change
        }
        val activity = change.activity as AbstractActivity
        val date = activity.start.inBoston().toLocalDate()
        val courtId = COURTS_BY_ID.inverse().getValue(activity.court)
        val slot =
            client.slotsTaken(date, date).singleOrNull {
                it.court == courtId &&
                    it.startUtc == activity.start.epochSecond
            }
                ?: return change.copy(action = Action.Delete)
        val expectedType = if (activity is Clinic) "lesson" else "match"
        if (slot.type != expectedType) {
            return change.copy(action = Action.Delete)
        }
        val match = activity as? Match ?: return change.copy(action = Action.Update)
        val players =
            client.reservation(slot.reservationId).players.filterNot {
                it.isMyself || it.type == "fill"
            }.map { player ->
                Player(name = player.text)
            }.toSet()
        return change.copy(action = Action.Update, activity = match.copy(players = players))
    }
}
