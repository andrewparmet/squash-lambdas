package com.parmet.squashlambdas.testutil

import com.google.api.services.calendar.Calendar
import com.parmet.squashlambdas.AppConfig
import com.parmet.squashlambdas.clublocker.ClubLockerClient
import com.parmet.squashlambdas.configureCalendar
import com.parmet.squashlambdas.configureClubLockerClient
import com.parmet.squashlambdas.loadConfiguration
import org.junit.jupiter.api.BeforeEach
import software.amazon.awssdk.services.s3.S3Client

abstract class ConfiguredTest {
    lateinit var config: AppConfig
    lateinit var calendar: Calendar
    internal lateinit var client: ClubLockerClient
    lateinit var email: String

    @BeforeEach
    fun before() {
        config = loadConfiguration("test.yml")
        calendar = configureCalendar(
            config,
            object : S3Client {
                override fun serviceName(): String =
                    "S3"
                override fun close() {}
            }
        )
        val clientAndEmail = configureClubLockerClient(
            config,
            object : S3Client {
                override fun serviceName(): String =
                    "S3"
                override fun close() {}
            }
        )
        client = clientAndEmail.first
        email = clientAndEmail.second.email!!
    }
}
