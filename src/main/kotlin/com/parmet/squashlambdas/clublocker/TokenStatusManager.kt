package com.parmet.squashlambdas.clublocker

import com.fasterxml.jackson.module.kotlin.readValue
import com.parmet.squashlambdas.ClubLockerConfig
import com.parmet.squashlambdas.json.Json
import com.parmet.squashlambdas.notify.Notifier
import io.github.oshai.kotlinlogging.KotlinLogging
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import javax.inject.Inject
import javax.inject.Named

private val logger = KotlinLogging.logger { }

class TokenStatusManager @Inject constructor(
    private val config: ClubLockerConfig,
    private val s3Client: S3Client,
    @param:Named("myNotifier") private val notifier: Notifier
) {
    fun isTokenValid(): Boolean {
        val storedToken = loadToken()
        return storedToken.isValid.also {
            if (!it) {
                logger.info { "Token is marked invalid (invalidTime: ${storedToken.invalidTime})" }
            }
        }
    }

    fun markTokenInvalid(reason: String) {
        val currentToken = loadToken()

        if (!currentToken.isValid) {
            logger.info { "Token already marked invalid, skipping" }
            return
        }

        val invalidatedToken = currentToken.invalidate()
        val json = Json.mapper.writeValueAsString(invalidatedToken)

        logger.info { "Marking token as invalid: $reason" }

        s3Client.putObject(
            { req ->
                req.bucket(config.token.bucket)
                req.key(config.token.key)
            },
            RequestBody.fromString(json)
        )

        logger.info { "Token marked invalid in S3" }
        notifier.publishTokenInvalidated(reason)
    }

    private fun loadToken(): StoredToken =
        s3Client.getObject {
            it.bucket(config.token.bucket)
            it.key(config.token.key)
        }.use { Json.mapper.readValue(it) }
}
