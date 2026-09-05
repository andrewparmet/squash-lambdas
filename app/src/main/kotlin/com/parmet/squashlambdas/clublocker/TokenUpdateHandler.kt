package com.parmet.squashlambdas.clublocker

import com.parmet.squashlambdas.TokenUpdateConfig
import com.parmet.squashlambdas.aws.ObjectStorage
import com.parmet.squashlambdas.email.EmailData
import com.parmet.squashlambdas.json.Json
import com.parmet.squashlambdas.notify.OperatorNotifier
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

@Inject
class TokenUpdateHandler(
    private val config: TokenUpdateConfig,
    private val objectStorage: ObjectStorage,
    private val notifier: OperatorNotifier
) {
    fun isTokenUpdateCandidate(email: EmailData) =
        email.subject == config.expectedSubject

    fun isTokenUpdateEmail(email: EmailData, senderAuthenticated: Boolean): Boolean {
        val senderMatches = config.expectedSender.equals(email.sender, ignoreCase = true)
        return isTokenUpdateCandidate(email) && senderMatches && senderAuthenticated
    }

    suspend fun handle(email: EmailData) {
        val token = email.body.trim()
        require(token.isNotEmpty()) { "Token update is empty" }
        require(token.length <= MAX_TOKEN_LENGTH) { "Token update exceeds the maximum length" }
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

    private companion object {
        const val MAX_TOKEN_LENGTH = 32_768
    }
}
