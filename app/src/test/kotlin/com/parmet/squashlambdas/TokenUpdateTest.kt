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
    fun `matches authenticated exact sender and subject`() {
        assertThat(handler.isTokenUpdateEmail(email(), true)).isTrue()
    }

    @Test
    fun `matches sender without case sensitivity`() {
        assertThat(handler.isTokenUpdateEmail(email(sender = "ME@EXAMPLE.COM"), true)).isTrue()
    }

    @Test
    fun `does not match with different case subject`() {
        assertThat(handler.isTokenUpdateEmail(email(subject = "CLUBLOCKER TOKEN"), true)).isFalse()
    }

    @Test
    fun `does not match when sender is different`() {
        assertThat(handler.isTokenUpdateEmail(email(sender = "other@example.com"), true)).isFalse()
    }

    @Test
    fun `does not match sender containing the expected address`() {
        assertThat(handler.isTokenUpdateEmail(email(sender = "attacker-me@example.com"), true)).isFalse()
    }

    @Test
    fun `does not match unauthenticated sender`() {
        assertThat(handler.isTokenUpdateEmail(email(), false)).isFalse()
    }

    @Test
    fun `does not match when subject is different`() {
        assertThat(
            handler.isTokenUpdateEmail(email(subject = "Tennis & Racquet Club Reservation Confirmation"), true)
        ).isFalse()
    }

    @Test
    fun `does not match when subject is partial match`() {
        assertThat(handler.isTokenUpdateEmail(email(subject = "ClubLocker Token Update"), true)).isFalse()
    }

    private fun email(sender: String = "me@example.com", subject: String = "ClubLocker Token") =
        EmailData(
            sender = sender,
            recipients = listOf("lambda@example.com"),
            subject = subject,
            body = "some-token",
            origin = "test",
            sesDkimAuthenticated = false,
        )
}
