package com.parmet.squashlambdas.infra

import aws.sdk.kotlin.runtime.auth.credentials.ProfileCredentialsProvider
import aws.sdk.kotlin.services.lambda.LambdaClient
import aws.sdk.kotlin.services.lambda.deleteFunction
import aws.sdk.kotlin.services.lambda.getFunctionConfiguration
import aws.sdk.kotlin.services.lambda.listAliases
import aws.sdk.kotlin.services.lambda.listVersionsByFunction
import aws.sdk.kotlin.services.resourcegroupstaggingapi.ResourceGroupsTaggingClient
import aws.sdk.kotlin.services.resourcegroupstaggingapi.getResources
import aws.sdk.kotlin.services.resourcegroupstaggingapi.model.TagFilter
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.ssm.SsmClient
import aws.sdk.kotlin.services.ssm.describeParameters
import aws.sdk.kotlin.services.ssm.getParameter
import aws.sdk.kotlin.services.ssm.model.ParameterStringFilter
import aws.sdk.kotlin.services.ssm.putParameter
import aws.smithy.kotlin.runtime.content.toByteArray

internal class Aws(private val profile: String, private val region: String) : AutoCloseable {
    private val credentials = ProfileCredentialsProvider(profileName = profile, region = region)
    private var lambdaClient: LambdaClient? = null
    private var s3Client: S3Client? = null
    private var ssmClient: SsmClient? = null
    private var taggingClient: ResourceGroupsTaggingClient? = null

    suspend fun bootstrapParameterArns(): List<String> {
        val arns = mutableListOf<String>()
        var paginationToken: String? = null
        do {
            val response =
                tagging().getResources {
                    resourceTypeFilters = listOf("ssm:parameter")
                    tagFilters =
                        listOf(
                            TagFilter {
                                key = "Application"
                                values = listOf("squash-lambdas")
                            },
                            TagFilter {
                                key = "Role"
                                values = listOf("terraform-bootstrap")
                            },
                        )
                    this.paginationToken = paginationToken
                }
            arns += response.resourceTagMappingList.orEmpty().mapNotNull { it.resourceArn }
            paginationToken = response.paginationToken?.takeIf { it.isNotBlank() }
        } while (paginationToken != null)
        return arns
    }

    suspend fun getParameter(name: String): String? =
        ssm().getParameter {
            this.name = name
            withDecryption = true
        }.parameter?.value

    suspend fun parameterMetadata(name: String) =
        requireNotNull(
            ssm().describeParameters {
                parameterFilters =
                    listOf(
                        ParameterStringFilter {
                            key = "Name"
                            values = listOf(name)
                        }
                    )
            }.parameters?.singleOrNull()
        )

    suspend fun putParameter(name: String, value: String) {
        val metadata = parameterMetadata(name)
        ssm().putParameter {
            this.name = name
            this.value = value
            type = metadata.type
            tier = metadata.tier
            keyId = metadata.keyId
            overwrite = true
        }
    }

    suspend fun readObject(bucket: String, key: String): ByteArray =
        s3().getObject(
            GetObjectRequest {
                this.bucket = bucket
                this.key = key
            }
        ) { response ->
            requireNotNull(response.body).toByteArray()
        }

    suspend fun aliases(functionName: String): Set<Int> {
        val versions = mutableSetOf<Int>()
        var marker: String? = null
        do {
            val response =
                lambda().listAliases {
                    this.functionName = functionName
                    this.marker = marker
                }
            response.aliases.orEmpty().forEach { alias ->
                alias.functionVersion?.toIntOrNull()?.let(versions::add)
                versions += alias.routingConfig?.additionalVersionWeights.orEmpty().keys.mapNotNull(String::toIntOrNull)
            }
            marker = response.nextMarker?.takeIf { it.isNotBlank() }
        } while (marker != null)
        return versions
    }

    suspend fun versions(functionName: String): List<Int> {
        val versions = mutableListOf<Int>()
        var marker: String? = null
        do {
            val response =
                lambda().listVersionsByFunction {
                    this.functionName = functionName
                    this.marker = marker
                }
            versions += response.versions.orEmpty().mapNotNull { it.version?.toIntOrNull() }
            marker = response.nextMarker?.takeIf { it.isNotBlank() }
        } while (marker != null)
        return versions
    }

    suspend fun deleteVersion(functionName: String, version: Int) {
        lambda().deleteFunction {
            this.functionName = functionName
            qualifier = version.toString()
        }
    }

    suspend fun snapshotStatus(functionName: String, version: String): SnapshotStatus =
        lambda().getFunctionConfiguration {
            this.functionName = functionName
            qualifier = version
        }.let { response ->
            SnapshotStatus(
                state = response.state?.value,
                stateReasonCode = response.stateReasonCode?.value,
                optimization = response.snapStart?.optimizationStatus?.value
            )
        }

    override fun close() {
        lambdaClient?.close()
        s3Client?.close()
        ssmClient?.close()
        taggingClient?.close()
        credentials.close()
    }

    private suspend fun lambda(): LambdaClient =
        lambdaClient ?: LambdaClient.fromEnvironment {
            region = this@Aws.region
            credentialsProvider = credentials
        }.also { lambdaClient = it }

    private suspend fun s3(): S3Client =
        s3Client ?: S3Client.fromEnvironment {
            region = this@Aws.region
            credentialsProvider = credentials
        }.also { s3Client = it }

    private suspend fun ssm(): SsmClient =
        ssmClient ?: SsmClient.fromEnvironment {
            region = this@Aws.region
            credentialsProvider = credentials
        }.also { ssmClient = it }

    private suspend fun tagging(): ResourceGroupsTaggingClient =
        taggingClient ?: ResourceGroupsTaggingClient.fromEnvironment {
            region = this@Aws.region
            credentialsProvider = credentials
        }.also { taggingClient = it }
}

internal data class SnapshotStatus(val state: String?, val stateReasonCode: String?, val optimization: String?)
