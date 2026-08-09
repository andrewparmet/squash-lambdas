package com.parmet.squashlambdas

import kotlinx.serialization.Serializable

@Serializable
data class EmailNotificationConfig(
    val googleCal: GoogleCalConfig,
    val sns: SnsConfig,
    val parse: ParseConfig,
    val tokenUpdate: TokenUpdateConfig
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
    val primaryRecipient: String
)
