package com.parmet.squashlambdas.email

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SesEmailEvent(
    @SerialName("Records") val records: List<SesEmailRecord>
)

@Serializable
data class SesEmailRecord(
    val ses: SesMessage
)

@Serializable
data class SesMessage(
    val mail: SesMail,
    val receipt: SesReceipt
)

@Serializable
data class SesMail(
    val messageId: String
)

@Serializable
data class SesReceipt(
    val recipients: List<String>,
    val spamVerdict: SesVerdict,
    val virusVerdict: SesVerdict,
    val dmarcVerdict: SesVerdict
) {
    val isSafe
        get() = spamVerdict.passed && virusVerdict.passed
}

@Serializable
data class SesVerdict(
    val status: String
) {
    val passed
        get() = status.equals("PASS", ignoreCase = true)
}
