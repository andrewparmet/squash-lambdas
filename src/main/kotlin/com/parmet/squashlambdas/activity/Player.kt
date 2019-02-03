package com.parmet.squashlambdas.activity

import com.google.common.base.Preconditions.checkArgument

internal data class Player
private constructor(
    val name: String? = null,
    val email: String? = null
) {
    init {
        checkArgument(!(name == null && email == null))
    }

    companion object {
        fun named(name: String) = Player(name = name)

        fun withEmail(email: String) = Player(email = email)
    }
}
