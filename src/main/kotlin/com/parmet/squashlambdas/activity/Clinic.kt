package com.parmet.squashlambdas.activity

import com.parmet.squashlambdas.email.EmailData
import java.time.Instant

internal data class Clinic(
    override val court: Court,
    override val start: Instant,
    override val end: Instant
) : AbstractActivity() {

    override fun summary() = "${court.sport} Clinic"

    companion object {
        fun fromEmailData(email: EmailData): Clinic {
            val startAndEnd = TimeParser.parse(email.body)
            return Clinic(
                Court.fromLocationString(email.body),
                startAndEnd.start,
                startAndEnd.end
            )
        }
    }
}
