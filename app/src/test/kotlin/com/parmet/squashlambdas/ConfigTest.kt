package com.parmet.squashlambdas

import org.junit.jupiter.api.Test

class ConfigTest {
    @Test
    fun `parse configs`() {
        loadConfiguration<MakeReservationConfig>("production-make-reservation-handler.conf")
        loadConfiguration<MonitorSlotsConfig>("production-monitor-slots-handler.conf")
    }
}
