package com.parmet.squashlambdas.email

import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import javax.inject.Inject
import javax.mail.internet.MimeMessage

class EmailRetriever @Inject constructor(
    private val s3: S3Client
) {
    fun retrieveEmail(bucket: String, key: String) =
        s3.getObjectAsBytes(
            GetObjectRequest.builder().bucket(bucket).key(key).build()
        ).asUtf8String().byteInputStream().use { stream ->
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
