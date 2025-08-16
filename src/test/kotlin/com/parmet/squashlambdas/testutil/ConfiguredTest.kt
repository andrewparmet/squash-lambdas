package com.parmet.squashlambdas.testutil

import com.google.api.services.calendar.Calendar
import com.parmet.squashlambdas.EmailNotificationConfig
import com.parmet.squashlambdas.MakeReservationConfig
import com.parmet.squashlambdas.MonitorSlotsConfig
import com.parmet.squashlambdas.clublocker.ClubLockerClient
import com.parmet.squashlambdas.configureCalendar
import com.parmet.squashlambdas.configureClubLockerResources
import com.parmet.squashlambdas.loadConfiguration
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach

abstract class ConfiguredTest {
    lateinit var emailNotificationConfig: EmailNotificationConfig
    lateinit var makeReservationConfig: MakeReservationConfig
    lateinit var monitorSlotsConfig: MonitorSlotsConfig
    lateinit var calendar: Calendar
    internal lateinit var client: ClubLockerClient

    @BeforeEach
    fun before() {
        emailNotificationConfig = loadConfiguration("test-email-notification-handler.yml")
        makeReservationConfig = loadConfiguration("test-make-reservation-handler.yml")
        monitorSlotsConfig = loadConfiguration("test-monitor-slots-handler.yml")
        calendar = configureCalendar(emailNotificationConfig.googleCal, mockk())
        val clubLockerResources = configureClubLockerResources(makeReservationConfig.clubLocker, mockk())
        client = clubLockerResources.client
    }
}
