package com.parmet.squashlambdas

data class EmailNotificationConfig(
    val googleCal: GoogleCalConfig,
    val sns: SnsConfig,
    val parse: ParseConfig
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
    val dynamoDb: DynamoConfig
)

data class GoogleCalConfig(
    val calendarId: String,
    val fileConfig: FileConfig
)

data class AwsClubLockerConfig(
    val creds: AwsFileConfig
)

data class AwsFileConfig(
    val bucket: String,
    val key: String
)

data class SnsConfig(
    val myTopicArn: String,
    val publicTopicArn: String
)

data class DynamoConfig(
    val squashSlotsTableName: String
)

data class ClubLockerConfig(
    val fileConfig: FileConfig,
    val name: String?
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
