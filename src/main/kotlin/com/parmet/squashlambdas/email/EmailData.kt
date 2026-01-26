package com.parmet.squashlambdas.email

data class EmailData(
    val sender: String,
    val recipients: List<String>,
    val subject: String,
    val body: String,
    val origin: String,
)
