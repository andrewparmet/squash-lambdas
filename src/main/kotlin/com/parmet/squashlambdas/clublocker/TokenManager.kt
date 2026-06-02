package com.parmet.squashlambdas.clublocker

import com.fasterxml.jackson.module.kotlin.readValue
import com.parmet.squashlambdas.ClubLockerConfig
import com.parmet.squashlambdas.json.Json
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging
import software.amazon.awssdk.services.s3.S3Client

private val logger = KotlinLogging.logger { }

@Inject
class TokenManager(
    private val config: ClubLockerConfig,
    private val s3Client: S3Client
) {
    private var cachedToken: String? = null
    private var cachedETag: String? = null

    fun getToken(): String {
        val currentETag = getETag()

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

    private fun getETag(): String =
        s3Client.headObject {
            it.bucket(config.token.bucket)
            it.key(config.token.key)
        }.eTag()

    private fun loadToken(): String {
        val storedToken: StoredToken = s3Client.getObject {
            it.bucket(config.token.bucket)
            it.key(config.token.key)
        }.use { Json.mapper.readValue(it) }

        logger.info { "Loaded token with updateTime: ${storedToken.updateTime}" }
        return storedToken.token
    }
}
