package com.parmet.squashlambdas.s3

import com.amazonaws.services.lambda.runtime.events.S3Event
import com.parmet.squashlambdas.json.InstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
internal data class S3EmailNotification(
    private val eventVersion: String,
    private val eventSource: String,
    private val awsRegion: String,
    @Serializable(with = InstantSerializer::class)
    private val eventTime: Instant,
    private val eventName: String,
    @SerialName("s3")
    val s3ObjectInfo: S3CreateObjectInfo
) {
    companion object {
        fun fromInputObject(input: S3Event) =
            input.records.first().run {
                S3EmailNotification(
                    eventVersion,
                    eventSource,
                    awsRegion,
                    Instant.parse(eventTime.toInstant().toString()),
                    eventName,
                    s3.run {
                        S3CreateObjectInfo(
                            bucket.run {
                                BucketInfo(
                                    name
                                )
                            },
                            `object`.run {
                                S3ObjectInfo(
                                    key
                                )
                            }
                        )
                    }
                )
            }
    }
}
