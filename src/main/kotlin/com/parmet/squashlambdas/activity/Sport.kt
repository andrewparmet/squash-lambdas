package com.parmet.squashlambdas.activity

import com.parmet.squashlambdas.notify.SportSerializer
import kotlinx.serialization.Serializable

@Serializable(with = SportSerializer::class)
sealed class Sport {
    override fun toString() =
        this::class.java.simpleName

    object Squash : Sport()

    object Hardball : Sport()

    object Tennis : Sport()

    object Racquets : Sport()

    object Fitness : Sport()
}
