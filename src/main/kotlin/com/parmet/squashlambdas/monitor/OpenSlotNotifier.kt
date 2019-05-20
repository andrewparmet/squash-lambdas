package com.parmet.squashlambdas.monitor

import com.amazonaws.services.pinpoint.AmazonPinpoint
import com.amazonaws.services.pinpoint.model.AddressConfiguration
import com.amazonaws.services.pinpoint.model.ChannelType
import com.amazonaws.services.pinpoint.model.DirectMessageConfiguration
import com.amazonaws.services.pinpoint.model.MessageRequest
import com.amazonaws.services.pinpoint.model.SMSMessage
import com.amazonaws.services.pinpoint.model.SendMessagesRequest
import com.parmet.squashlambdas.clublocker.COURTS_BY_ID
import com.parmet.squashlambdas.clublocker.Slot
import mu.KotlinLogging

internal class OpenSlotNotifier(
    private val pinpoint: AmazonPinpoint,
    private val applicationId: String
) {
    private val logger = KotlinLogging.logger { }

    fun notifyOpenSlots(slots: List<Slot>, users: List<User>) {
        require(slots.isNotEmpty())
        require(users.isNotEmpty())

        pinpoint.sendMessages(
            SendMessagesRequest()
                .withApplicationId(applicationId)
                .withMessageRequest(
                    MessageRequest()
                        .withAddresses(
                            users.associate {
                                it.phoneNumber to AddressConfiguration().withChannelType(ChannelType.SMS)
                            }
                        )
                        .withMessageConfiguration(
                            DirectMessageConfiguration()
                                .withSMSMessage(
                                    SMSMessage()
                                        .withBody(
                                            "Open slots found:\n" +
                                                slots.joinToString("\n") { prettyPrint(it) })
                                )
                        )
                )
        ).also {
            logger.info { "Pinpoint message send result: $it" }
        }
    }

    private fun prettyPrint(slot: Slot) =
        "Court: ${COURTS_BY_ID.getValue(slot.court).pretty}, Time: " +
            "${TimeFormatter.formatTime(slot.startTime)}-${TimeFormatter.formatTime(slot.endTime)}"
}
