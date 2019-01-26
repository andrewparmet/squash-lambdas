package com.parmet.squashlambdas.email

import biweekly.component.VEvent
import biweekly.property.Attendee
import biweekly.property.Organizer
import com.google.common.truth.Truth.assertThat
import com.parmet.squashlambdas.integration.IntegrationTests
import com.parmet.squashlambdas.testutil.EmailReturningS3
import com.parmet.squashlambdas.testutil.getResourceAsString
import java.sql.Date
import java.time.Instant
import org.junit.Test

class EmailRetrieverTest {
    @Test
    fun `retrieveEmail properly deserializes S3 object`() {
        val s3 =
            EmailReturningS3(
                getResourceAsString(IntegrationTests::class.java, "reservationCreated2"))
        val data = EmailRetriever(s3, "", "").retrieveEmail()
        assertThat(data).isEqualTo(emailData())
    }

    companion object {
        fun event() =
            VEvent().apply {
                setDateTimeStamp(Date.from(Instant.parse("2018-03-26T00:27:36Z")))
                setUid("741228ef-faa3-4e59-9b1e-ce8b2aefca5a")
                setDateStart(Date.from(Instant.parse("2018-03-29T01:00:00Z")))
                setDateEnd(Date.from(Instant.parse("2018-03-29T01:45:00Z")))
                setSummary("Reservation for Hardball")
                setOrganizer(Organizer("Tennis & Racquet Club", "no-reply@ussquash.com"))
                setLocation("Tennis & Racquet Club / Court: Court #7")
                addAttendee(attendee())
            }

        fun attendee() =
            Attendee("Andrew Parmet", "joecool@peanuts.com").apply {
                addParameter("CUTYPE", "INDIVIDUAL")
            }

        fun emailData() =
            EmailData(
                "Tennis & Racquet Club Reservation Confirmation",
                """
                    Hello Andrew Parmet, A reservation including you has been made via the Tennis & Racquet
                    Club court reservation system. Reservation details: Court: Court #7 - Hardball
                    Date: Wednesday, March 28th 2018 Time: 09:00 PM to 09:45 PM To cancel your spot
                    or the whole reservation please log into Club Locker and use the My Reservations
                    area.
                """.trimIndent().replace("\n", " "),
                event())

        fun fromBody(body: String) =
            EmailRetriever(
                EmailReturningS3(body),
                "parmet-squash-emails",
                "emails/some-file-name")
                .retrieveEmail()
    }
}
