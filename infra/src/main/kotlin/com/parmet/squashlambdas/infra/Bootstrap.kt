package com.parmet.squashlambdas.infra

import kotlinx.serialization.json.JsonObject

internal class Bootstrap(private val aws: Aws) {
    suspend fun load(): LoadedBootstrap {
        val name = parameterName()
        val value = aws.getParameter(name) ?: error("Unable to read the Terraform bootstrap parameter")
        return LoadedBootstrap(name, parseObject(value, "bootstrap parameter"))
    }

    suspend fun update(bootstrap: LoadedBootstrap, value: JsonObject) {
        aws.putParameter(bootstrap.parameterName, value.toString())
    }

    private suspend fun parameterName(): String {
        val arns = aws.bootstrapParameterArns()
        check(arns.size == 1) { "Expected exactly one Terraform bootstrap parameter, found ${arns.size}" }
        val parameterName = arns.single().substringAfter(":parameter", missingDelimiterValue = "")
        check(parameterName.startsWith("/")) { "The tagged Terraform bootstrap resource is not an SSM parameter" }
        return parameterName
    }
}

internal data class LoadedBootstrap(val parameterName: String, val value: JsonObject)
