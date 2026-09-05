package com.parmet.squashlambdas.clublocker

internal class ClubLockerHttpException(
    val statusCode: Int
) : RuntimeException("Club Locker returned HTTP $statusCode")
