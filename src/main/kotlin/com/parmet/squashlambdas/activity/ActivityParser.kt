package com.parmet.squashlambdas.activity

import com.parmet.squashlambdas.email.EmailData

class ActivityParser(private val email: EmailData) {
    fun parseStartAndEnd() = TimeParser.parse(email.body)

    fun parseCourt() = Court.fromLocationString(email.body)

    fun parseOtherPlayers() = OtherPlayersParser.parse(email.body)
}
