package com.parmet.squashlambdas.infra

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.AclRule
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.io.ByteArrayInputStream
import java.nio.file.Path
import java.security.MessageDigest
import com.google.api.services.calendar.model.Calendar as GoogleCalendar

private class ProvisionCalendar : SuspendingCliktCommand(name = "provision-calendar") {
    private val repositoryDirectory by argument().path(mustExist = true, canBeFile = false)
    private val shareWith by option("--share-with").multiple()

    override suspend fun run() {
        CalendarProvisioner(repositoryDirectory).provision(shareWith)
    }
}

private class CalendarProvisioner(private val repositoryDirectory: Path) {
    private val profile = System.getenv("AWS_PROFILE") ?: "personal"
    private val region = System.getenv("TF_VAR_aws_region") ?: "us-east-1"
    private val commands = CommandRunner(repositoryDirectory)
    private val aws = Aws(profile, region)
    private val bootstrap = Bootstrap(aws)

    suspend fun provision(shareWith: List<String>) {
        try {
            commands.awsLogin(profile, region)
            val loadedBootstrap = bootstrap.load()
            val configuration = loadedBootstrap.value
            val resourceNames = configuration.requiredObject("resource_names")
            val privateConfig = configuration.requiredObject("private_config")
            val credentialsKey = privateConfig.requiredString("google_calendar_credentials_key")
            val receiver = privateConfig.requiredString("parse_primary_recipient")
            val calendar = loadGoogleCalendar(resourceNames.requiredString("bucket"), credentialsKey)
            val configuredId = privateConfig["google_calendar_id"]?.jsonPrimitive?.contentOrNull

            if (!configuredId.isNullOrBlank() && configuredId != "primary") {
                setCalendarName(calendar, configuredId)
                storeGoogleConfiguration(loadedBootstrap, configuration, privateConfig, configuredId)
                shareWith.forEach { share(calendar, configuredId, it) }
                println("The dedicated calendar is already configured")
                return
            }

            val marker = "squash-lambdas:${receiver.sha256()}"
            val existingCalendars =
                withContext(Dispatchers.IO) {
                    calendar.calendarList().list().execute().items.orEmpty().filter { it.description == marker }
                }
            check(existingCalendars.size <= 1) { "More than one dedicated calendar exists for the configured receiver" }
            val calendarId =
                existingCalendars.singleOrNull()?.id
                    ?: withContext(Dispatchers.IO) {
                        calendar.calendars().insert(
                            GoogleCalendar()
                                .setSummary("T&R")
                                .setDescription(marker)
                                .setTimeZone("America/New_York")
                        ).execute().id
                    }

            storeGoogleConfiguration(loadedBootstrap, configuration, privateConfig, calendarId)
            shareWith.forEach { share(calendar, calendarId, it) }
            println("Created and configured the dedicated calendar")
        } finally {
            aws.close()
        }
    }

    private suspend fun setCalendarName(calendar: Calendar, calendarId: String) =
        withContext(Dispatchers.IO) {
            val configured = calendar.calendars().get(calendarId).execute()
            if (configured.summary != "T&R") {
                calendar.calendars().patch(calendarId, GoogleCalendar().setSummary("T&R")).execute()
            }
        }

    private suspend fun share(calendar: Calendar, calendarId: String, user: String) =
        withContext(Dispatchers.IO) {
            val rule =
                calendar.acl().list(calendarId).execute().items.orEmpty().singleOrNull {
                    it.scope?.type == "user" && it.scope?.value.equals(user, ignoreCase = true)
                }
            if (rule?.role != "writer") {
                val writer = AclRule().setRole("writer").setScope(AclRule.Scope().setType("user").setValue(user))
                if (rule == null) {
                    calendar.acl().insert(calendarId, writer).execute()
                } else {
                    calendar.acl().update(calendarId, requireNotNull(rule.id), writer).execute()
                }
            }
        }

    private suspend fun storeGoogleConfiguration(
        loadedBootstrap: LoadedBootstrap,
        configuration: JsonObject,
        privateConfig: JsonObject,
        calendarId: String
    ) {
        val updatedPrivateConfig =
            JsonObject(
                privateConfig + ("google_calendar_id" to JsonPrimitive(calendarId))
            )
        if (updatedPrivateConfig != privateConfig) {
            bootstrap.update(loadedBootstrap, JsonObject(configuration + ("private_config" to updatedPrivateConfig)))
        }
    }

    private suspend fun loadGoogleCalendar(bucket: String, credentialsKey: String): Calendar {
        val credentialBytes = aws.readObject(bucket, credentialsKey)
        return withContext(Dispatchers.IO) {
            val credentials =
                ByteArrayInputStream(credentialBytes).use {
                    GoogleCredentials.fromStream(it).createScoped(listOf(CalendarScopes.CALENDAR))
                }
            Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory(),
                HttpCredentialsAdapter(credentials)
            ).setApplicationName("PARMET_SQUASH_LAMBDAS").build()
        }
    }
}

private fun String.sha256(): String =
    MessageDigest.getInstance("SHA-256").digest(toByteArray()).joinToString("") { "%02x".format(it) }

suspend fun main(args: Array<String>) =
    ProvisionCalendar().main(args)
