package com.parmet.squashlambdas.email

import dev.zacsweers.metro.Inject
import software.amazon.awssdk.services.s3.S3Client
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

@Inject
class EmailRetriever(
    private val s3: S3Client
) {
    fun retrieveEmail(bucket: String, key: String) =
        s3.getObject {
            it.bucket(bucket)
            it.key(key)
        }.use { stream ->
            val message = MimeMessage(null, stream)
            EmailData(
                message.from?.firstOrNull()?.toString() ?: "",
                message.recipients(),
                message.subject,
                BodyExtractor.extract(message).toString(),
                key,
            )
        }
}

private fun MimeMessage.recipients() =
    (
        allRecipients.orEmpty().asSequence() +
            getHeader("X-Forwarded-To").orEmpty().asSequence()
                .flatMap { InternetAddress.parseHeader(it, false).asSequence() }
        ).map { it.toString() }
        .distinct()
        .toList()
