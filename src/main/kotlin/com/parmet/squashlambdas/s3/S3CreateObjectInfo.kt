package com.parmet.squashlambdas.s3
import com.fasterxml.jackson.annotation.JsonProperty

internal data class S3CreateObjectInfo(
    private val bucket: BucketInfo,
    @param:JsonProperty("object")
    private val objectInfo: S3ObjectInfo
) {
    val bucketName
        get() = bucket.name

    val objectKey
        get() = objectInfo.key
}
