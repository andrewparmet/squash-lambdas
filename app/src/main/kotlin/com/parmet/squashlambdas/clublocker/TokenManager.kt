package com.parmet.squashlambdas.clublocker

import com.parmet.squashlambdas.ClubLockerConfig
import com.parmet.squashlambdas.aws.ObjectStorage
import com.parmet.squashlambdas.json.Json
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

@Inject
class TokenManager(
    private val config: ClubLockerConfig,
    private val objectStorage: ObjectStorage
) {
    private var cachedToken: String? = null
    private var cachedETag: String? = null

    suspend fun getToken(): String {
        val currentETag = objectStorage.eTag(requireNotNull(config.token.bucket), requireNotNull(config.token.key))

        if (cachedToken != null && cachedETag == currentETag) {
            logger.info { "Token unchanged (ETag: $currentETag), using cached token" }
            return cachedToken!!
        }

        logger.info { "Token changed or not cached (cached ETag: $cachedETag, current: $currentETag), loading from S3" }
        return loadToken().also {
            cachedToken = it
            cachedETag = currentETag
        }
    }

    private suspend fun loadToken(): String {
        val storedToken: StoredToken =
            Json.decode(
                objectStorage.read(
                    requireNotNull(config.token.bucket),
                    requireNotNull(config.token.key)
                ).decodeToString()
            )

        logger.info { "Loaded token with updateTime: ${storedToken.updateTime}" }
        return storedToken.token
    }
}
