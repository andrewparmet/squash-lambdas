package com.parmet.squashlambdas.s3

import com.google.gson.annotations.SerializedName

internal data class S3CreateObjectInfo(
    private val bucket: BucketInfo,
    @SerializedName("object")
    private val objectInfo: S3ObjectInfo
) {
    val bucketName
        get() = bucket.name

    val objectKey
        get() = objectInfo.key
}
