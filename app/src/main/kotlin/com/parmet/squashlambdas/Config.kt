package com.parmet.squashlambdas

import kotlinx.serialization.Serializable

@Serializable
data class EmailNotificationConfig(
    val clubLocker: ClubLockerConfig,
    val googleCal: GoogleCalConfig,
    val sns: SnsConfig,
    val parse: ParseConfig,
    val tokenUpdate: TokenUpdateConfig
)

@Serializable
data class EmailRoutingConfig(
    val bucket: String,
    val clubLockerEmail: String,
    val clubLockerTokenKey: String,
    val googleCalendarCredentialsKey: String,
    val notificationTopicArn: String,
    val tokenUpdateExpectedSender: String,
    val tokenUpdateExpectedSubject: String,
    val tenants: Map<String, EmailTenantConfig>
) {
    fun applicationConfig(tenant: EmailTenantConfig) =
        EmailNotificationConfig(
            clubLocker =
            ClubLockerConfig(
                token = FileConfig(location = "s3", bucket = bucket, key = clubLockerTokenKey),
                email = clubLockerEmail
            ),
            googleCal =
            GoogleCalConfig(
                calendarId = tenant.googleCalendarId,
                creds = FileConfig(location = "s3", bucket = bucket, key = googleCalendarCredentialsKey)
            ),
            sns = SnsConfig(myTopicArn = notificationTopicArn),
            parse =
            ParseConfig(
                primaryRecipient = tenant.primaryRecipient,
                inboundEmailBucket = bucket,
                inboundEmailPrefix = tenant.inboundEmailPrefix
            ),
            tokenUpdate =
            TokenUpdateConfig(
                expectedSender = tokenUpdateExpectedSender,
                expectedSubject = tokenUpdateExpectedSubject,
                tokenDestination = FileConfig(location = "s3", bucket = bucket, key = clubLockerTokenKey)
            )
        )
}

@Serializable
data class EmailTenantConfig(
    val inboundRecipients: List<String>,
    val inboundEmailPrefix: String,
    val primaryRecipient: String,
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

@Serializable
data class ParseConfig(
    val primaryRecipient: String,
    val inboundEmailBucket: String,
    val inboundEmailPrefix: String
)
