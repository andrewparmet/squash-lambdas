package com.parmet.squashlambdas.email

import com.parmet.squashlambdas.aws.ObjectStorage
import dev.zacsweers.metro.Inject
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeUtility

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
                message.sesDkimAuthenticated(),
            )
        }
}

private fun MimeMessage.sesDkimAuthenticated(): Boolean {
    val authenticationResults =
        getHeader("Authentication-Results")?.firstOrNull()?.let(MimeUtility::unfold) ?: return false
    val authority = authenticationResults.substringBefore(';').trim()
    return authority.equals("amazonses.com", ignoreCase = true) &&
        Regex("""(?:^|[\s;])dkim=pass(?:[\s;(]|$)""", RegexOption.IGNORE_CASE).containsMatchIn(authenticationResults)
}

private fun MimeMessage.senderAddress() =
    (from?.firstOrNull() as? InternetAddress)?.address?.lowercase().orEmpty()

private fun MimeMessage.recipients() =
    allRecipients.orEmpty().asSequence()
        .mapNotNull { (it as? InternetAddress)?.address?.lowercase() }
        .distinct()
        .toList()
