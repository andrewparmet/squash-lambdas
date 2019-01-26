package com.parmet.squashlambdas.cal

import com.parmet.squashlambdas.testutil.ConfiguredTest
import org.junit.Ignore
import org.junit.Test

class CalUtilsTest : ConfiguredTest() {
    @Test
    @Ignore
    fun `give a new user ownership of the config's calendarId`() {
        printAcl(calendar(), config().getString("google.calendarId"))
        giveUserOwnership(calendar(), config().getString("google.calendarId"), "")
        printAcl(calendar(), config().getString("google.calendarId"))
    }
}
