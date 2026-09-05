package com.parmet.squashlambdas.s3
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class S3CreateObjectInfo(
    private val bucket: BucketInfo,
    @SerialName("object")
    private val objectInfo: S3ObjectInfo
) {
    val bucketName
        get() = bucket.name

    val objectKey
        get() = objectInfo.key
}
