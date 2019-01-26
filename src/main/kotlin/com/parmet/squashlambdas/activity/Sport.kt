package com.parmet.squashlambdas.activity

sealed class Sport {
    override fun toString() = this::class.java.name

    object Squash : Sport()
    object Hardball : Sport()
    object Tennis : Sport()
    object Racquets : Sport()
}
