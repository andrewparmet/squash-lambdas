package com.parmet.squashlambdas.email

import biweekly.component.VEvent

data class EmailData(
    val subject: String,
    val body: String,
    val event: VEvent?,
    val csvAttachment: String? = null
)
