package com.parmet.squashlambdas

data class AppConfig(
    val google: GoogleConfig,
    val aws: AwsConfig,
    val clubLocker: ClubLockerConfig,
    val schedule: FileConfig,
    val courts: FileConfig,
    val times: FileConfig,
    val matchfind: MatchFindConfig,
    val parse: ParseConfig
)

data class GoogleConfig(
    val cal: CalendarConfig
)

data class CalendarConfig(
    val creds: FileConfig,
    val calendarId: String,
)

data class AwsConfig(
    val google: AwsGoogleConfig,
    val clubLocker: AwsClubLockerConfig,
    val schedule: AwsFileConfig,
    val courts: AwsFileConfig,
    val times: AwsFileConfig,
    val sns: SnsConfig,
    val dynamo: DynamoConfig
)

data class AwsGoogleConfig(
    val cal: AwsCalendarConfig
)

data class AwsCalendarConfig(
    val creds: AwsFileConfig
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
    val creds: FileConfig,
    val name: String
)

data class FileConfig(
    val location: String,
    val fileName: String
)

data class MatchFindConfig(
    val recipient: String
)

data class ParseConfig(
    val primaryRecipient: String
)
