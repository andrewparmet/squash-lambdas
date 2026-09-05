package com.parmet.squashlambdas

import kotlinx.serialization.Serializable

@Serializable
data class EmailNotificationConfig(
    val clubLocker: ClubLockerConfig,
    val googleCal: GoogleCalConfig,
    val sns: SnsConfig,
    val tokenUpdate: TokenUpdateConfig
)

@Serializable
data class EmailRoutingConfig(
    val bucket: String,
    val inboundEmailPrefix: String,
    val inboundRecipient: String,
    val clubLockerEmail: String,
    val clubLockerTokenKey: String,
    val googleCalendarCredentialsKey: String,
    val notificationTopicArn: String,
    val calendarExpectedSender: String,
    val tokenUpdateExpectedSender: String,
    val tokenUpdateExpectedSubject: String,
    val tenants: Map<String, EmailTenantConfig>
) {
    fun applicationConfig(tenant: EmailTenantConfig): EmailNotificationConfig {
        val token = FileConfig(location = "s3", bucket = bucket, key = clubLockerTokenKey)
        return EmailNotificationConfig(
            clubLocker =
            ClubLockerConfig(
                token = token,
                email = clubLockerEmail
            ),
            googleCal =
            GoogleCalConfig(
                calendarId = tenant.googleCalendarId,
                creds = FileConfig(location = "s3", bucket = bucket, key = googleCalendarCredentialsKey)
            ),
            sns = SnsConfig(myTopicArn = notificationTopicArn),
            tokenUpdate =
            TokenUpdateConfig(
                expectedSender = tokenUpdateExpectedSender,
                expectedSubject = tokenUpdateExpectedSubject,
                tokenDestination = token
            )
        )
    }
}

@Serializable
data class EmailTenantConfig(
    val forwardedRecipient: String,
    val googleCalendarId: String
)

@Serializable
data class TokenUpdateConfig(
    val expectedSender: String,
    val expectedSubject: String,
    val tokenDestination: FileConfig
)

@Serializable
data class MakeReservationConfig(
    val clubLocker: ClubLockerConfig,
    val schedule: FileConfig,
    val courts: FileConfig,
    val times: FileConfig,
    val sns: SnsConfig
)

@Serializable
data class MonitorSlotsConfig(
    val clubLocker: ClubLockerConfig,
    val sns: SnsConfig,
    val dynamoDb: DynamoDbConfig
)

@Serializable
data class GoogleCalConfig(
    val calendarId: String,
    val creds: FileConfig
)

@Serializable
data class SnsConfig(
    val myTopicArn: String,
    val publicTopicArn: String? = null
)

@Serializable
data class DynamoDbConfig(
    val squashSlotsTableName: String
)

@Serializable
data class ClubLockerConfig(
    val token: FileConfig,
    val name: String? = null,
    val email: String
)

@Serializable
data class FileConfig(
    val location: String,
    val bucket: String? = null,
    val key: String? = null,
    val fileName: String? = null
)
