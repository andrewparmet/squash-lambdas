package com.parmet.squashlambdas.clublocker

import com.parmet.squashlambdas.TokenUpdateConfig
import com.parmet.squashlambdas.email.EmailData
import com.parmet.squashlambdas.json.Json
import com.parmet.squashlambdas.notify.Notifier
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Named
import io.github.oshai.kotlinlogging.KotlinLogging
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client

private val logger = KotlinLogging.logger { }

@Inject
class TokenUpdateHandler(
    private val config: TokenUpdateConfig,
    private val s3Client: S3Client,
    @param:Named("myNotifier") private val notifier: Notifier
) {
    fun isTokenUpdateEmail(email: EmailData): Boolean {
        val senderMatches = config.expectedSender in email.sender
        val subjectMatches = email.subject == config.expectedSubject
        return senderMatches && subjectMatches
    }

    fun handle(email: EmailData) {
        val token = email.body.trim()
        val storedToken = StoredToken.create(token)
        val json = Json.mapper.writeValueAsString(storedToken)

        logger.info { "Received token update email, storing to S3" }

        s3Client.putObject(
            { req ->
                req.bucket(config.tokenDestination.bucket)
                req.key(config.tokenDestination.key)
            },
            RequestBody.fromString(json)
        )

        logger.info { "Token stored successfully" }
        notifier.publishTokenUpdated(storedToken.updateTime)
    }
}
