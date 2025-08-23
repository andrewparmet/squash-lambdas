package com.parmet.squashlambdas.testutil

import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import java.util.function.Consumer

class EmailReturningS3(
    private val obj: String
) : S3Client {
    override fun getObject(getObjectRequest: Consumer<GetObjectRequest.Builder>) =
        ResponseInputStream(GetObjectResponse.builder().build(), obj.byteInputStream())

    override fun serviceName() =
        "S3"

    override fun close() =
        Unit
}
