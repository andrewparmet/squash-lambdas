package com.parmet.squashlambdas.activity

import com.parmet.squashlambdas.email.EmailData
import com.parmet.squashlambdas.json.InstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
@SerialName("Lesson")
data class Lesson(
    override val court: Court,
    @Serializable(with = InstantSerializer::class)
    override val start: Instant,
    @Serializable(with = InstantSerializer::class)
    override val end: Instant,
    override val origin: String,
    val coach: String
) : AbstractActivity() {
    override fun summary() =
        "${court.sport} Lesson with $coach"

    companion object {
        private val coachPattern =
            Regex("lesson(?:\\s+you\\s+have)?\\s+with\\s+(.+?)\\s+at\\s+", RegexOption.IGNORE_CASE)

        fun fromEmailData(email: EmailData): Lesson {
            val startAndEnd = TimeParser.parse(email.body)
            val coach = requireNotNull(coachPattern.find(email.body)?.groupValues?.get(1)) {
                "Unable to parse lesson coach"
            }
            return Lesson(
                Court.fromLocationString(email.body),
                startAndEnd.start,
                startAndEnd.end,
                email.origin,
                coach
            )
        }
    }
}
