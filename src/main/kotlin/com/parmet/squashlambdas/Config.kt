package com.parmet.squashlambdas

data class EmailNotificationConfig(
    val googleCal: GoogleCalConfig,
    val sns: SnsConfig,
    val parse: ParseConfig,
    val tokenUpdate: TokenUpdateConfig
)

data class TokenUpdateConfig(
    val expectedSender: String,
    val expectedSubject: String,
    val tokenDestination: FileConfig
)

data class MakeReservationConfig(
    val clubLocker: ClubLockerConfig,
    val schedule: FileConfig,
    val courts: FileConfig,
    val times: FileConfig,
    val sns: SnsConfig
)

data class MonitorSlotsConfig(
    val clubLocker: ClubLockerConfig,
    val sns: SnsConfig,
    val dynamoDb: DynamoDbConfig
)

data class GoogleCalConfig(
    val calendarId: String,
    val creds: FileConfig
)

data class SnsConfig(
    val myTopicArn: String,
    val publicTopicArn: String?
)

data class DynamoDbConfig(
    val squashSlotsTableName: String
)

data class ClubLockerConfig(
    val token: FileConfig,
    val name: String?,
    val email: String
)

data class FileConfig(
    val location: String,
    val bucket: String?,
    val key: String?,
    val fileName: String?
)

data class ParseConfig(
    val primaryRecipient: String
)
