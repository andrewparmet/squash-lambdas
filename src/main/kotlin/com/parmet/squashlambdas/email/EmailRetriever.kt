package com.parmet.squashlambdas.email

import com.amazonaws.services.s3.AmazonS3
import java.nio.charset.StandardCharsets.UTF_8
import javax.mail.internet.MimeMessage

class EmailRetriever(private val s3: AmazonS3) {
    fun retrieveEmail(bucket: String, key: String) =
        s3.getObjectAsString(bucket, key).byteInputStream(UTF_8).use {
            val message = MimeMessage(null, it)
            EmailData(
                message.subject,
                BodyExtractor.extract(message).toString(),
                CalendarExtractor.extract(message).flatMap { it.events }.firstOrNull())
        }
}
