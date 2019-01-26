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
            val retriever = ActivityParser(email)
            val startAndEnd = retriever.parseStartAndEnd()
            return Match(
                retriever.parseCourt(),
                startAndEnd.start,
                startAndEnd.end,
                retriever.parseOtherPlayers())
        }
    }
}