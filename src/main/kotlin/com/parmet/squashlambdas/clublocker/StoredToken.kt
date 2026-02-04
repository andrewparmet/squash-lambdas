package com.parmet.squashlambdas.clublocker

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.Instant

data class StoredToken(
    val token: String,
    val updateTime: Instant,
    val invalidTime: Instant? = null
) {
    @get:JsonIgnore
    val isValid: Boolean
        get() = invalidTime == null

    fun invalidate(): StoredToken =
        copy(invalidTime = Instant.now())

    companion object {
        fun create(token: String) =
            StoredToken(token, Instant.now())
    }
}
