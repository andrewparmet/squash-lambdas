package com.parmet.squashlambdas.clublocker

import java.time.Instant

data class StoredToken(
    val token: String,
    val updateTime: Instant,
    val invalidTime: Instant? = null
) {
    val isValid: Boolean
        get() = invalidTime == null

    fun invalidate(): StoredToken =
        copy(invalidTime = Instant.now())

    companion object {
        fun create(token: String) =
            StoredToken(token, Instant.now())
    }
}
