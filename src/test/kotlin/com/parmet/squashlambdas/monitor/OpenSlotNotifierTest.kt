package com.parmet.squashlambdas.monitor

import com.parmet.squashlambdas.clublocker.COURTS_BY_ID
import com.parmet.squashlambdas.clublocker.Slot
import com.parmet.squashlambdas.configurePinpoint
import com.parmet.squashlambdas.testutil.ConfiguredTest
import org.junit.Test

class OpenSlotNotifierTest : ConfiguredTest() {
    @Test
    fun `test sending message`() {
        OpenSlotNotifier(configurePinpoint(), config.getString("aws.pinpoint.applicationId"))
            .notifyOpenSlots(
                listOf(
                    Slot(1, 1, COURTS_BY_ID.keys.first(), 1800, 1845, System.currentTimeMillis().toInt()),
                    Slot(1, 1, COURTS_BY_ID.keys.toList()[1], 1845, 1930, System.currentTimeMillis().toInt())
                ),
                listOf(User("Me", "test@test.com", "+1-555-555-5555"))
            )
    }
}
