package com.parmet.squashlambdas.cal

import com.parmet.squashlambdas.testutil.ConfiguredTest
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class CalUtilsTest : ConfiguredTest() {
    @Test
    @Disabled
    fun `give a new user ownership of the config's calendarId`() {
        printAcl(calendar, config.google.cal.calendarId)
        giveUserOwnership(calendar, config.google.cal.calendarId, "")
        printAcl(calendar, config.google.cal.calendarId)
    }
}
