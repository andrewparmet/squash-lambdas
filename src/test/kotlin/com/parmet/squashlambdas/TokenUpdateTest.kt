package com.parmet.squashlambdas

import com.google.common.truth.Truth.assertThat
import com.parmet.squashlambdas.clublocker.TokenUpdateHandler
import com.parmet.squashlambdas.email.EmailData
import io.mockk.mockk
import org.junit.jupiter.api.Test

class TokenUpdateTest {
    private val config =
        TokenUpdateConfig(
            expectedSender = "me@example.com",
            expectedSubject = "ClubLocker Token",
            tokenDestination = FileConfig("s3", "bucket", "key", null)
        )

    private val handler = TokenUpdateHandler(config, mockk(), mockk())

    @Test
    fun `matches when sender contains expected and subject matches exactly`() {
        val email =
            EmailData(
                sender = "Me <me@example.com>",
                recipients = listOf("lambda@server.com"),
                subject = "ClubLocker Token",
                body = "some-token",
                origin = "test"
            )

        assertThat(handler.isTokenUpdateEmail(email)).isTrue()
    }

    @Test
    fun `does not match with different case sender`() {
        val email =
            EmailData(
                sender = "ME@EXAMPLE.COM",
                recipients = listOf("lambda@server.com"),
                subject = "ClubLocker Token",
                body = "some-token",
                origin = "test"
            )

        assertThat(handler.isTokenUpdateEmail(email)).isFalse()
    }

    @Test
    fun `does not match with different case subject`() {
        val email =
            EmailData(
                sender = "me@example.com",
                recipients = listOf("lambda@server.com"),
                subject = "CLUBLOCKER TOKEN",
                body = "some-token",
                origin = "test"
            )

        assertThat(handler.isTokenUpdateEmail(email)).isFalse()
    }

    @Test
    fun `does not match when sender is different`() {
        val email =
            EmailData(
                sender = "other@example.com",
                recipients = listOf("lambda@server.com"),
                subject = "ClubLocker Token",
                body = "some-token",
                origin = "test"
            )

        assertThat(handler.isTokenUpdateEmail(email)).isFalse()
    }

    @Test
    fun `does not match when subject is different`() {
        val email =
            EmailData(
                sender = "me@example.com",
                recipients = listOf("lambda@server.com"),
                subject = "Tennis & Racquet Club Reservation Confirmation",
                body = "some content",
                origin = "test"
            )

        assertThat(handler.isTokenUpdateEmail(email)).isFalse()
    }

    @Test
    fun `does not match when subject is partial match`() {
        val email =
            EmailData(
                sender = "me@example.com",
                recipients = listOf("lambda@server.com"),
                subject = "ClubLocker Token Update",
                body = "some-token",
                origin = "test"
            )

        assertThat(handler.isTokenUpdateEmail(email)).isFalse()
    }
}
