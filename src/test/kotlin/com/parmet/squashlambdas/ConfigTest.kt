package com.parmet.squashlambdas

import org.junit.jupiter.api.Test

class ConfigTest {
    @Test
    fun `parse configs`() {
        loadConfiguration<EmailNotificationConfig>("production-email-notification-handler.conf")
        loadConfiguration<MakeReservationConfig>("production-make-reservation-handler.conf")
        loadConfiguration<MonitorSlotsConfig>("production-monitor-slots-handler.conf")
    }
}
