package com.parmet.squashlambdas.activity

import com.parmet.squashlambdas.email.EmailData
import java.time.Instant

data class Clinic(
    @Transient
    override val court: Court,
    @Transient
    override val start: Instant,
    @Transient
    override val end: Instant
) : AbstractActivity(start, end, court) {

    override fun summary() = "${court.sport} Clinic"

    companion object {
        fun fromEmailData(email: EmailData): Clinic {
            val retriever = ActivityParser(email)
            val startAndEnd = retriever.parseStartAndEnd()
            return Clinic(
                retriever.parseCourt(),
                startAndEnd.start,
                startAndEnd.end)
        }
    }
}
