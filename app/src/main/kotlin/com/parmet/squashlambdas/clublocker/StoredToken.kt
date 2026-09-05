package com.parmet.squashlambdas.clublocker

import com.parmet.squashlambdas.json.InstantSerializer
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class StoredToken(
    val token: String,
    @Serializable(with = InstantSerializer::class)
    val updateTime: Instant,
    @Serializable(with = InstantSerializer::class)
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
