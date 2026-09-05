package com.parmet.squashlambdas.clublocker

import com.parmet.squashlambdas.TokenUpdateConfig
import com.parmet.squashlambdas.aws.ObjectStorage
import com.parmet.squashlambdas.email.EmailData
import com.parmet.squashlambdas.json.Json
import com.parmet.squashlambdas.notify.Notifier
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Named
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

@Inject
class TokenUpdateHandler(
    private val config: TokenUpdateConfig,
    private val objectStorage: ObjectStorage,
    @param:Named("myNotifier") private val notifier: Notifier
) {
    fun isTokenUpdateEmail(email: EmailData): Boolean {
        val senderMatches = config.expectedSender in email.sender
        val subjectMatches = email.subject == config.expectedSubject
        return senderMatches && subjectMatches
    }

    suspend fun handle(email: EmailData) {
        val token = email.body.trim()
        val storedToken = StoredToken.create(token)
        val json = Json.encode(storedToken)

        logger.info { "Received token update email, storing to S3" }

        objectStorage.write(
            requireNotNull(config.tokenDestination.bucket),
            requireNotNull(config.tokenDestination.key),
            json.encodeToByteArray(),
        )

        logger.info { "Token stored successfully" }
        notifier.publishTokenUpdated(storedToken.updateTime)
    }
}
