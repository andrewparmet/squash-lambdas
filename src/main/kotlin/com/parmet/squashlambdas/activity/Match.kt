package com.parmet.squashlambdas.activity

import com.parmet.squashlambdas.email.EmailData
import com.parmet.squashlambdas.json.InstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
@SerialName("Match")
data class Match(
    override val court: Court,
    @Serializable(with = InstantSerializer::class)
    override val start: Instant,
    @Serializable(with = InstantSerializer::class)
    override val end: Instant,
    override val origin: String,
    val players: Set<Player>
) : AbstractActivity() {
    override fun summary() =
        "${court.sport} ${renderOtherPlayers()}"

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
