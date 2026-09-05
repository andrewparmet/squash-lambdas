package com.parmet.squashlambdas.testutil

import com.parmet.squashlambdas.aws.ObjectStorage

class EmailReturningS3(
    private val obj: String
) : ObjectStorage {
    override suspend fun read(bucket: String, key: String) =
        obj.encodeToByteArray()

    override suspend fun write(bucket: String, key: String, contents: ByteArray) =
        error("not supported")

    override suspend fun eTag(bucket: String, key: String): String =
        error("not supported")
}
