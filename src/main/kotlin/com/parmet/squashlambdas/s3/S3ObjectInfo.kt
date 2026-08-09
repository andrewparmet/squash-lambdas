package com.parmet.squashlambdas.s3

import kotlinx.serialization.Serializable

@Serializable
internal data class S3ObjectInfo(
    val key: String
)
