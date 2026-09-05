package com.parmet.squashlambdas.email

import com.parmet.squashlambdas.aws.ObjectStorage
import dev.zacsweers.metro.Inject
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage

@Inject
class EmailRetriever(
    private val objectStorage: ObjectStorage
) {
    suspend fun retrieveEmail(bucket: String, key: String) =
        objectStorage.read(bucket, key).inputStream().use { stream ->
            val message = MimeMessage(null, stream)
            EmailData(
                message.senderAddress(),
                message.recipients(),
                message.subject,
                BodyExtractor.extract(message).toString(),
                key,
            )
        }
}

private fun MimeMessage.senderAddress() =
    (from?.firstOrNull() as? InternetAddress)?.address?.lowercase().orEmpty()

private fun MimeMessage.recipients() =
    (
        allRecipients.orEmpty().asSequence() +
            getHeader("X-Forwarded-To").orEmpty().asSequence()
                .flatMap { InternetAddress.parseHeader(it, false).asSequence() }
        ).mapNotNull { (it as? InternetAddress)?.address?.lowercase() }
        .distinct()
        .toList()
