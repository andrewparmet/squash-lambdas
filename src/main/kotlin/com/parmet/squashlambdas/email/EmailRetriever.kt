package com.parmet.squashlambdas.email

import com.amazonaws.services.s3.AmazonS3
import java.nio.charset.StandardCharsets.UTF_8
import javax.mail.internet.MimeMessage

class EmailRetriever(
    private val s3: AmazonS3,
    private val bucket: String,
    private val key: String
) {
    fun retrieveEmail(): EmailData {
        val email = s3.getObjectAsString(bucket, key)
        email.byteInputStream(UTF_8).use {
            val message = MimeMessage(null, it)
            return EmailData(
                message.subject,
                BodyExtractor.extract(message).toString(),
                CalendarExtractor.extract(message).flatMap { it.events }.firstOrNull())
        }
    }
}
