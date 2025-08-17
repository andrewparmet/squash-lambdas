package com.parmet.squashlambdas.email

import software.amazon.awssdk.services.s3.S3Client
import javax.inject.Inject
import javax.mail.internet.MimeMessage

class EmailRetriever @Inject constructor(
    private val s3: S3Client
) {
    fun retrieveEmail(bucket: String, key: String) =
        s3.getObject {
            it.bucket(bucket)
            it.key(key)
        }.use { stream ->
            val message = MimeMessage(null, stream)
            EmailData(
                message.allRecipients.map { it.toString() },
                message.subject,
                BodyExtractor.extract(message).toString(),
                key,
            )
        }
}
