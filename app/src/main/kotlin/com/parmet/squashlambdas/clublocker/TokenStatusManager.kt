package com.parmet.squashlambdas.clublocker

import com.parmet.squashlambdas.ClubLockerConfig
import com.parmet.squashlambdas.aws.ObjectStorage
import com.parmet.squashlambdas.json.Json
import com.parmet.squashlambdas.notify.Notifier
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Named
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

@Inject
class TokenStatusManager(
    private val config: ClubLockerConfig,
    private val objectStorage: ObjectStorage,
    @param:Named("myNotifier") private val notifier: Notifier
) {
    suspend fun isTokenValid(): Boolean {
        val storedToken = loadToken()
        return storedToken.isValid.also {
            if (!it) {
                logger.info { "Token is marked invalid (invalidTime: ${storedToken.invalidTime})" }
            }
        }
    }

    suspend fun markTokenInvalid(reason: String) {
        val currentToken = loadToken()

        if (!currentToken.isValid) {
            logger.info { "Token already marked invalid, skipping" }
            return
        }

        val invalidatedToken = currentToken.invalidate()
        val json = Json.encode(invalidatedToken)

        logger.info { "Marking token as invalid: $reason" }

        objectStorage.write(
            requireNotNull(config.token.bucket),
            requireNotNull(config.token.key),
            json.encodeToByteArray(),
        )

        logger.info { "Token marked invalid in S3" }
        notifier.publishTokenInvalidated(reason)
    }

    private suspend fun loadToken(): StoredToken =
        Json.decode(
            objectStorage.read(requireNotNull(config.token.bucket), requireNotNull(config.token.key)).decodeToString()
        )
}
