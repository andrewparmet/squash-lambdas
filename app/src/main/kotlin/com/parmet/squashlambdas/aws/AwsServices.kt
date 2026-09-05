package com.parmet.squashlambdas.aws

import aws.sdk.kotlin.hll.dynamodbmapper.DynamoDbMapper
import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.headObject
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.putObject
import aws.sdk.kotlin.services.sns.SnsClient
import aws.sdk.kotlin.services.sns.publish
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.content.toByteArray

interface ObjectStorage {
    suspend fun read(bucket: String, key: String): ByteArray

    suspend fun write(bucket: String, key: String, contents: ByteArray)

    suspend fun eTag(bucket: String, key: String): String
}

internal class S3ObjectStorage : ObjectStorage {
    private var client: S3Client? = null

    override suspend fun read(bucket: String, key: String): ByteArray =
        client().getObject(
            GetObjectRequest {
                this.bucket = bucket
                this.key = key
            }
        ) { response ->
            requireNotNull(response.body).toByteArray()
        }

    override suspend fun write(bucket: String, key: String, contents: ByteArray) {
        client().putObject {
            this.bucket = bucket
            this.key = key
            body = ByteStream.fromBytes(contents)
        }
    }

    override suspend fun eTag(bucket: String, key: String): String =
        requireNotNull(
            client().headObject {
                this.bucket = bucket
                this.key = key
            }.eTag
        )

    private suspend fun client() =
        client ?: S3Client.fromEnvironment().also { client = it }
}

interface TopicPublisher {
    suspend fun publish(topicArn: String, subject: String, message: String)
}

internal class SnsTopicPublisher : TopicPublisher {
    private var client: SnsClient? = null

    override suspend fun publish(topicArn: String, subject: String, message: String) {
        client().publish {
            this.topicArn = topicArn
            this.subject = subject
            this.message = message
        }
    }

    private suspend fun client() =
        client ?: SnsClient.fromEnvironment().also { client = it }
}

class DynamoDbMapperProvider {
    private var mapper: DynamoDbMapper? = null

    suspend fun get(): DynamoDbMapper =
        mapper ?: DynamoDbMapper(DynamoDbClient.fromEnvironment()).also { mapper = it }
}
