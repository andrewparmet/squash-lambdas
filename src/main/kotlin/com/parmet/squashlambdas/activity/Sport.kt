package com.parmet.squashlambdas.activity

internal sealed class Sport {
    override fun toString() = this::class.java.simpleName

    object Squash : Sport()
    object Hardball : Sport()
    object Tennis : Sport()
    object Racquets : Sport()
}
