package com.parmet.squashlambdas.activity

import com.google.common.truth.Truth.assertThat
import com.parmet.squashlambdas.integration.emailnotification.emailFromBody
import org.junit.jupiter.api.Test

class OtherPlayersParserTest {
    @Test
    fun `an email with no players can be parsed correctly`() {
        val body = emailFromBody("reservationCreated2").body

        assertThat(OtherPlayersParser.parse(body)).isEmpty()
    }

    @Test
    fun `an email with a player can be parsed correctly`() {
        val body = emailFromBody("reservationCreated3").body

        assertThat(OtherPlayersParser.parse(body)).containsExactly("Philipp Rimmler")
    }
}
