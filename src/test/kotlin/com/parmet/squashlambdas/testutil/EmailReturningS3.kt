package com.parmet.squashlambdas.testutil

import com.amazonaws.services.s3.AbstractAmazonS3

class EmailReturningS3(
    private val obj: String
) : AbstractAmazonS3() {
    override fun getObjectAsString(bucket: String, key: String) =
        obj
}
