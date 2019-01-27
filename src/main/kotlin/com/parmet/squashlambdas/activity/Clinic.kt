package com.parmet.squashlambdas.activity

import com.parmet.squashlambdas.email.EmailData
import java.time.Instant

data class Clinic(
    private val court: Court,
    private val start: Instant,
    private val end: Instant
) : AbstractActivity(start, end, court) {

    override fun summary() = "${court.sport} Clinic"

    companion object {
        fun fromEmailData(email: EmailData): Clinic {
            val startAndEnd = TimeParser.parse(email.body)
            return Clinic(
                Court.fromLocationString(email.body),
                startAndEnd.start,
                startAndEnd.end)
        }
    }
}
