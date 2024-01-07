package com.parmet.squashlambdas.email

import com.amazonaws.services.s3.AmazonS3
import javax.mail.internet.MimeMessage

class EmailRetriever(private val s3: AmazonS3) {
    fun retrieveEmail(bucket: String, key: String) =
        s3.getObjectAsString(bucket, key).byteInputStream().use { stream ->
            val message = MimeMessage(null, stream)
            EmailData(
                message.allRecipients.map { it.toString() },
                message.subject,
                BodyExtractor.extract(message).toString(),
                CalendarExtractor.extract(message).flatMap { it.events }.firstOrNull(),
                key,
                CsvExtractor.extract(message).toString().takeIf { it.isNotEmpty() }
            )
        }
}
