package com.parmet.squashlambdas.testutil

import com.amazonaws.services.s3.AbstractAmazonS3
import com.google.api.services.calendar.Calendar
import com.parmet.squashlambdas.configureCalendar
import com.parmet.squashlambdas.configureClubLockerClient
import com.parmet.squashlambdas.loadConfiguration
import com.parmet.squashlambdas.clublocker.ClubLockerClient
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
        calendar = configureCalendar(config, object : AbstractAmazonS3() {})
        val clientAndEmail = configureClubLockerClient(config, object : AbstractAmazonS3() {})
        client = clientAndEmail.first
        email = clientAndEmail.second.email!!
    }
}
