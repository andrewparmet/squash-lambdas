package com.parmet.squashlambdas.activity

import com.google.api.services.calendar.model.Event
import com.parmet.squashlambdas.email.EmailData
import kotlinx.serialization.Serializable

@Serializable
sealed interface Activity {
    fun toEvent(): Event

    fun searchString(): String

    fun summary(): String

    companion object {
        fun fromEmailData(email: EmailData) =
            when {
                email.isLesson() -> Lesson.fromEmailData(email)
                email.isClinic() -> Clinic.fromEmailData(email)
                else -> Match.fromEmailData(email)
            }

        private fun EmailData.isLesson() =
            subject.contains("Lesson", ignoreCase = true) ||
                body.contains("Lesson details:", ignoreCase = true) ||
                body.contains("With coach:", ignoreCase = true)

        private fun EmailData.isClinic() =
            body.contains("Clinic")
    }
}
