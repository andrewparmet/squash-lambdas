package com.parmet.squashlambdas.email

import biweekly.component.VEvent

data class EmailData(
    val recipients: List<String>,
    val subject: String,
    val body: String,
    val event: VEvent?,
    val origin: String,
    val csvAttachment: String?
)
