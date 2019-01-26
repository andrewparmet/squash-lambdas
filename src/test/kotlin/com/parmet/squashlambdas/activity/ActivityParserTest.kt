package com.parmet.squashlambdas.activity

import com.google.common.truth.Truth.assertThat
import com.parmet.squashlambdas.email.EmailRetrieverTest
import com.parmet.squashlambdas.integration.IntegrationTests
import com.parmet.squashlambdas.testutil.getResourceAsString
import java.time.Instant
import org.junit.Test

class ActivityParserTest {
    @Test
    fun `raw email data can be parsed into activity components`() {
        val data = EmailRetrieverTest.emailData()
        val parser = ActivityParser(data)

        assertThat(parser.parseCourt()).isEqualTo(Court.Court7)
        assertThat(parser.parseStartAndEnd().start)
            .isEqualTo(Instant.parse("2018-03-29T01:00:00Z"))
        assertThat(parser.parseStartAndEnd().end)
            .isEqualTo(Instant.parse("2018-03-29T01:45:00Z"))
        assertThat(parser.parseOtherPlayers()).isEmpty()
    }

    @Test
    fun `raw email data from file can be parsed into activity components`() {
        val parser =
            ActivityParser(
                EmailRetrieverTest.fromBody(
                    getResourceAsString(
                        IntegrationTests::class.java,
                        "reservationCreated2")))

        assertThat(parser.parseCourt()).isEqualTo(Court.Court7)
        assertThat(parser.parseStartAndEnd().start)
            .isEqualTo(Instant.parse("2018-03-29T01:00:00Z"))
        assertThat(parser.parseStartAndEnd().end)
            .isEqualTo(Instant.parse("2018-03-29T01:45:00Z"))
        assertThat(parser.parseOtherPlayers()).isEmpty()
    }
}
