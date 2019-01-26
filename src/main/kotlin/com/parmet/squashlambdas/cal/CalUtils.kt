package com.parmet.squashlambdas.cal

import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.AclRule
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

fun giveUserOwnership(calendar: Calendar, calendarId: String, userEmail: String) {
    calendar
        .acl()
        .insert(
            calendarId,
            AclRule()
                .setRole("owner")
                .setScope(AclRule.Scope().setType("user").setValue(userEmail)))
        .execute()
}

fun printAcl(calendar: Calendar, calendarId: String) {
    calendar.acl()
        .list(calendarId)
        .execute()
        .items
        .forEach {
            logger.info { "${it.id}:${it.role}" }
        }
}
