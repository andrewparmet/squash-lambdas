package com.parmet.squashlambdas.clublocker

import java.time.Instant

data class StoredToken(
    val token: String,
    val updateTime: Instant
) {
    companion object {
        fun create(token: String) =
            StoredToken(token, Instant.now())
    }
}
