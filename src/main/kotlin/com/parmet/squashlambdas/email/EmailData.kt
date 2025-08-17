package com.parmet.squashlambdas.email

data class EmailData(
    val recipients: List<String>,
    val subject: String,
    val body: String,
    val origin: String,
)
