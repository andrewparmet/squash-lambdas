package com.parmet.squashlambdas.integration.matchfind

import com.parmet.squashlambdas.configureSes
import com.parmet.squashlambdas.integration.emailnotification.emailFromBody
import com.parmet.squashlambdas.matchfind.CsvEmailSender
import com.parmet.squashlambdas.matchfind.CsvType
import org.junit.Test

class IntegrationTests {
    @Test
    fun `reading a csv from an email`() {
        val csvString = emailFromBody("matchfind").csvAttachment!!
        val squash = CsvType.SQUASH.filterCsv(csvString)
        val tennis = CsvType.TENNIS.filterCsv(csvString)

        val sender = CsvEmailSender(configureSes())
        val result = sender.send(squash, tennis, "andrew@parmet.com")
        println(result)
    }
}
