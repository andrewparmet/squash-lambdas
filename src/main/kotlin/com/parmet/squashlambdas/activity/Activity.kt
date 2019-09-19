package com.parmet.squashlambdas.activity

import com.google.api.services.calendar.model.Event
import com.google.common.base.Preconditions.checkArgument
import com.parmet.squashlambdas.email.EmailData

interface Activity {
    fun toEvent(): Event

    fun searchString(): String

    companion object {
        fun fromEmailData(email: EmailData) =
            if (email.isMatch()) {
                Match.fromEmailData(email)
            } else {
                checkArgument(email.isClinic(), "unknown activity type: %s", email)
                Clinic.fromEmailData(email)
            }

        // This is imperfect.
        private fun EmailData.isMatch() = !isClinic()

        // This seems to be reliable.
        private fun EmailData.isClinic() = body.contains("Clinic")
    }
}
