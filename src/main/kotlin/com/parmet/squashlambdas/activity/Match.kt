package com.parmet.squashlambdas.activity

import com.parmet.squashlambdas.email.EmailData
import java.time.Instant

data class Match(
    override val court: Court,
    override val start: Instant,
    override val end: Instant,
    override val origin: String,
    val players: Set<Player>
) : AbstractActivity() {

    override fun summary() =
        "${court.sport} ${renderOtherPlayers()}" +
            "\n\nOrigin: $origin"

    private fun otherPlayers() =
        players.filter { it.name != "Parmet, Andrew" && it.name != "Andrew Parmet" }

    private fun renderOtherPlayers() =
        if (otherPlayers().isEmpty()) {
            "Match"
        } else {
            "v. ${otherPlayers().joinToString(",") { it.name!! }}"
        }

    companion object {
        fun fromEmailData(email: EmailData): Match {
            val startAndEnd = TimeParser.parse(email.body)
            return Match(
                Court.fromLocationString(email.body),
                startAndEnd.start,
                startAndEnd.end,
                email.origin,
                OtherPlayersParser.parse(email.body)
                    .map { Player(name = it) }
                    .toSet()
            )
        }
    }
}
