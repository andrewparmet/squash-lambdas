package com.parmet.squashlambdas.testutil

import software.amazon.awssdk.core.ResponseBytes
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse

class EmailReturningS3(
    private val obj: String
) : S3Client {
    override fun getObjectAsBytes(getObjectRequest: GetObjectRequest): ResponseBytes<GetObjectResponse> =
        ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), obj.toByteArray())

    override fun serviceName(): String = "S3"
    override fun close() {}
}
