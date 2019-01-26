package com.parmet.squashlambdas.s3

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.parmet.squashlambdas.testutil.getJsonResourceAsString
import java.time.Instant
import org.junit.Test

class S3EmailNotificationTest {
    @Test
    fun `fromInputObject deserializes properly`() {
        val obj = inputFromFile("s3CreateObject.json")
        val note = S3EmailNotification.fromInputObject(obj)
        val expected =
            S3EmailNotification(
                "2.0",
                "aws:s3",
                "us-east-1",
                Instant.parse("2018-03-26T00:27:50.117Z"),
                "ObjectCreated:Put",
                S3CreateObjectInfo(
                    BucketInfo("parmet-squash-emails"),
                    S3ObjectInfo("emails/srnvood65ihat8t9o7m1p1fjaiqumeq4do1p3bo1")))

        assertThat(note).isEqualTo(expected)
    }

    @Test
    fun `deserialized object has non-null s3ObjectInfo`() {
        val obj = inputFromFile("s3CreateObject.json")
        val note = S3EmailNotification.fromInputObject(obj)

        assertThat(note.s3ObjectInfo).isNotNull()
    }

    companion object {
        private val GSON = Gson()

        fun inputFromFile(filename: String): Any =
            GSON.fromJson(
                getJsonResourceAsString(filename),
                object : TypeToken<Map<String, Any>>() {}.type)
    }
}
