package com.parmet.squashlambdas.integration.matchfind

import com.parmet.squashlambdas.configureSes
import com.parmet.squashlambdas.integration.emailnotification.emailFromBody
import com.parmet.squashlambdas.matchfind.CsvEmailSender
import com.parmet.squashlambdas.matchfind.CsvType
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class IntegrationTests {
    @Test
    @Disabled
    fun `reading a csv from an email with text_csv`() {
        run("matchfind")
    }

    @Test
    @Disabled
    fun `reading a csv from an email with application_octet_stream`() {
        run("matchfindOctetStream")
    }

    @Test
    @Disabled
    fun `reading a csv from an email with application_vnd_ms_excel`() {
        run("matchfindVndMsExcel")
    }

    private fun run(fileName: String) {
        val csvString = emailFromBody(fileName).csvAttachment!!
        val squash = CsvType.SQUASH.filterCsv(csvString)
        val tennis = CsvType.TENNIS.filterCsv(csvString)

        val sender = CsvEmailSender(configureSes())
        val result = sender.send(squash, tennis, "andrew@parmet.com")
        println(result)
    }
}
