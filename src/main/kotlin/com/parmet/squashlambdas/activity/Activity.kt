package com.parmet.squashlambdas.activity

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.google.api.services.calendar.model.Event
import com.parmet.squashlambdas.email.EmailData

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = Match::class, name = "Match"),
    JsonSubTypes.Type(value = Clinic::class, name = "Clinic")
)
interface Activity {
    fun toEvent(): Event

    fun searchString(): String

    fun summary(): String

    companion object {
        fun fromEmailData(email: EmailData) =
            if (email.isMatch()) {
                Match.fromEmailData(email)
            } else {
                require(email.isClinic()) { "unknown activity type: $email" }
                Clinic.fromEmailData(email)
            }

        // This is imperfect.
        private fun EmailData.isMatch() =
            !isClinic()

        // This seems to be reliable.
        private fun EmailData.isClinic() =
            body.contains("Clinic")
    }
}
