package com.parmet.squashlambdas.activity

data class Player(
    val name: String? = null,
    val email: String? = null,
    val memberId: Int? = null
) {
    init {
        require(!(name == null && email == null && memberId == null))
    }
}
