package com.parmet.squashlambdas.clublocker

internal class ClubLockerHttpException(
    val statusCode: Int,
    val reasonPhrase: String
) : RuntimeException("status code: $statusCode, reason phrase: $reasonPhrase")
