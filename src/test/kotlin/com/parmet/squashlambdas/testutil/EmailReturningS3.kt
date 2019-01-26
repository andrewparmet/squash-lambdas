package com.parmet.squashlambdas.testutil

import com.amazonaws.services.s3.AmazonS3
import com.bizo.awsstubs.services.s3.AmazonS3Stub

class EmailReturningS3(private val obj: String) : AmazonS3 by AmazonS3Stub() {
    override fun getObjectAsString(bucket: String, key: String) = obj
}
