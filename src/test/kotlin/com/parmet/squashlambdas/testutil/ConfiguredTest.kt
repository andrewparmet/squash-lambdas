package com.parmet.squashlambdas.testutil

import com.bizo.awsstubs.services.s3.AmazonS3Stub
import com.google.api.services.calendar.Calendar
import com.parmet.squashlambdas.configureCalendar
import com.parmet.squashlambdas.configureClubLockerClient
import com.parmet.squashlambdas.loadConfiguration
import com.parmet.squashlambdas.reserve.ClubLockerClient
import org.apache.commons.configuration2.Configuration
import org.junit.Before

abstract class ConfiguredTest {
    lateinit var config: Configuration
    lateinit var calendar: Calendar
    internal lateinit var client: ClubLockerClient
    lateinit var email: String

    @Before
    fun before() {
        config = loadConfiguration("test.xml")
        calendar = configureCalendar(config, AmazonS3Stub())
        val clientAndEmail = configureClubLockerClient(config, AmazonS3Stub())
        client = clientAndEmail.first
        email = clientAndEmail.second.email!!
    }
}
