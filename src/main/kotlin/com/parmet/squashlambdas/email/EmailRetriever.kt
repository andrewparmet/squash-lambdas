package com.parmet.squashlambdas.email

import com.amazonaws.services.s3.AmazonS3
import com.google.common.base.Strings
import java.nio.charset.StandardCharsets.UTF_8
import javax.mail.internet.MimeMessage

class EmailRetriever(private val s3: AmazonS3) {
    fun retrieveEmail(bucket: String, key: String) =
        s3.getObjectAsString(bucket, key).byteInputStream(UTF_8).use { stream ->
            val message = MimeMessage(null, stream)
            EmailData(
                message.subject,
                BodyExtractor.extract(message).toString(),
                CalendarExtractor.extract(message).flatMap { it.events }.firstOrNull(),
                Strings.emptyToNull(CsvExtractor.extract(message).toString())
            )
        }
}
