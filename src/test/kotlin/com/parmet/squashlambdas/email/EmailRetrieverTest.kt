package com.parmet.squashlambdas.email

import com.google.common.truth.Truth.assertThat
import com.parmet.squashlambdas.cal.ChangeSummaryTest
import com.parmet.squashlambdas.testutil.EmailReturningS3
import com.parmet.squashlambdas.testutil.getResourceAsString
import org.junit.jupiter.api.Test

class EmailRetrieverTest {
    @Test
    fun `retrieveEmail properly deserializes S3 object`() {
        val s3 =
            EmailReturningS3(
                getResourceAsString(ChangeSummaryTest::class.java, "reservationCreated2"),
            )
        val data = EmailRetriever(s3).retrieveEmail("", "some-object-key")
        assertThat(data).isEqualTo(emailData())
    }
}

fun emailData() =
    EmailData(
        listOf("joecool@peanuts.com"),
        "Tennis & Racquet Club Reservation Confirmation",
        """
            Hello Andrew Parmet, A reservation including you has been made via the Tennis & Racquet
            Club court reservation system. Reservation details: Court: Court #7 - Hardball
            Date: Wednesday, March 28th 2018 Time: 09:00 PM to 09:45 PM To cancel your spot
            or the whole reservation please log into Club Locker and use the My Reservations
            area.
        """.trimIndent().replace("\n", " "),
        "some-object-key",
    )
