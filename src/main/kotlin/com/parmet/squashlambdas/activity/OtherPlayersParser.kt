package com.parmet.squashlambdas.activity

import mu.KotlinLogging
import java.util.regex.Pattern

internal object OtherPlayersParser {
    private val logger = KotlinLogging.logger { }

    private val HAS_JOINED =
        Pattern.compile(".*Parmet, (.*) has joined your reservation.*")

    private val OTHER_PLAYERS =
        Pattern.compile(".* Other players in the slot: (.*) Court:.*")

    // Name can be followed by an email in parentheses.
    private val WITH =
        Pattern.compile(".* With: (.*) (\\(.*\\))? Court:.*")

    fun parse(body: String): Set<String> {
        val hasJoined = HAS_JOINED.matcher(body)
        if (hasJoined.matches()) {
            return setOf(hasJoined.group(1))
        } else {
            val otherPlayers = OTHER_PLAYERS.matcher(body)
            return if (otherPlayers.matches()) {
                setOf(otherPlayers.group(1))
            } else {
                val with = WITH.matcher(body)
                if (with.matches()) {
                    setOf(with.group(1))
                } else {
                    logger.debug { "Did not find other players in body $body" }
                    setOf()
                }
            }
        }
    }
}
