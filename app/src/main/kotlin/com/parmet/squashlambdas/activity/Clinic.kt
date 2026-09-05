package com.parmet.squashlambdas.activity

import com.parmet.squashlambdas.email.EmailData
import com.parmet.squashlambdas.json.InstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
@SerialName("Clinic")
data class Clinic(
    override val court: Court,
    @Serializable(with = InstantSerializer::class)
    override val start: Instant,
    @Serializable(with = InstantSerializer::class)
    override val end: Instant,
    override val origin: String
) : AbstractActivity() {
    override fun summary() =
        "${court.sport} Clinic"

    companion object {
        fun fromEmailData(email: EmailData): Clinic {
            val startAndEnd = TimeParser.parse(email.body)
            return Clinic(
                Court.fromLocationString(email.body),
                startAndEnd.start,
                startAndEnd.end,
                email.origin
            )
        }
    }
}
