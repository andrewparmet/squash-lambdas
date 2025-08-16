package com.parmet.squashlambdas

import org.junit.jupiter.api.Test

class ConfigTest {
    @Test
    fun `parse configs`() {
        loadConfiguration<EmailNotificationConfig>("production-email-notification-handler.yml")
        loadConfiguration<MakeReservationConfig>("production-make-reservation-handler.yml")
        loadConfiguration<MonitorSlotsConfig>("production-monitor-slots-handler.yml")
    }
}
