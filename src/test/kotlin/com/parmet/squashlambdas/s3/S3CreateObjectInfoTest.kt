package com.parmet.squashlambdas.s3

import com.google.common.truth.Truth.assertThat
import com.parmet.squashlambdas.s3.S3EmailNotificationTest.Companion.inputFromFile
import org.junit.Test

class S3CreateObjectInfoTest {
    @Test
    fun `deserialized object has non-null bucketName and objectKey`() {
        val obj = inputFromFile("s3CreateObject.json")
        val objectInfo = S3EmailNotification.fromInputObject(obj).s3ObjectInfo

        assertThat(objectInfo.bucketName).isNotNull()
        assertThat(objectInfo.objectKey).isNotNull()
    }
}
