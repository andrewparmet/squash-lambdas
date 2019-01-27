package com.parmet.squashlambdas.activity

import com.parmet.squashlambdas.email.EmailData
import java.time.Instant

data class Match(
    private val court: Court,
    private val start: Instant,
    private val end: Instant,
    private val otherPlayers: Set<String>
) : AbstractActivity(start, end, court) {

    override fun summary() = "${court.sport} ${renderOtherPlayers()}"

    private fun renderOtherPlayers() =
        if (otherPlayers.isEmpty()) {
            "Match"
        } else {
            "v. ${otherPlayers.joinToString(",")}"
        }

    companion object {
        fun fromEmailData(email: EmailData): Match {
            val startAndEnd = TimeParser.parse(email.body)
            return Match(
                Court.fromLocationString(email.body),
                startAndEnd.start,
                startAndEnd.end,
                OtherPlayersParser.parse(email.body))
        }
    }
}
