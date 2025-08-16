package com.parmet.squashlambdas.testutil

import com.google.api.services.calendar.Calendar
import com.parmet.squashlambdas.EmailNotificationConfig
import com.parmet.squashlambdas.MakeReservationConfig
import com.parmet.squashlambdas.MonitorSlotsConfig
import com.parmet.squashlambdas.clublocker.ClubLockerClient
import com.parmet.squashlambdas.configureCalendar
import com.parmet.squashlambdas.configureClubLockerClient
import com.parmet.squashlambdas.loadConfiguration
import org.junit.jupiter.api.BeforeEach
import software.amazon.awssdk.services.s3.S3Client

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
        calendar = configureCalendar(
            emailNotificationConfig.googleCal,
            object : S3Client {
                override fun serviceName(): String =
                    "S3"
                override fun close() {}
            }
        )
        val clientAndEmail = configureClubLockerClient(
            makeReservationConfig.clubLocker,
            object : S3Client {
                override fun serviceName(): String =
                    "S3"
                override fun close() {}
            }
        )
        client = clientAndEmail.first
    }
}
