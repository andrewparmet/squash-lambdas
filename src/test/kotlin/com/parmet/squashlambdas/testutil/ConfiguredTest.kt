package com.parmet.squashlambdas.testutil

import com.bizo.awsstubs.services.s3.AmazonS3Stub
import com.google.api.services.calendar.Calendar
import com.parmet.squashlambdas.configureCalendar
import com.parmet.squashlambdas.loadConfiguration
import org.apache.commons.configuration2.Configuration
import org.junit.Before

abstract class ConfiguredTest {
    private lateinit var config: Configuration
    private lateinit var calendar: Calendar

    protected fun calendar() = calendar

    protected fun config() = config

    @Before
    fun before() {
        config = loadConfiguration("test.xml")
        calendar = configureCalendar(config, AmazonS3Stub())
    }
}
