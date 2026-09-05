package com.parmet.squashlambdas.infra

import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
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
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.ByteArrayInputStream
import java.nio.file.Path
import java.security.MessageDigest
import com.google.api.services.calendar.model.Calendar as GoogleCalendar

private const val CALENDAR_NAME = "T&R"

private class ProvisionUser : SuspendingCliktCommand(name = "provision-user") {
    private val repositoryDirectory by argument().path(mustExist = true, canBeFile = false)
    private val forwardedRecipient by option("--forwarded-recipient").required()
    private val shareWith by option("--share-with").multiple()

    override suspend fun run() {
        UserProvisioner(repositoryDirectory).provision(forwardedRecipient, shareWith)
    }
}

private class UserProvisioner(private val repositoryDirectory: Path) {
    private val profile = System.getenv("AWS_PROFILE") ?: "personal"
    private val region = System.getenv("TF_VAR_aws_region") ?: "us-east-1"
    private val commands = CommandRunner(repositoryDirectory)
    private val aws = Aws(profile, region)
    private val bootstrap = Bootstrap(aws)

    suspend fun provision(forwardedRecipient: String, calendarEditors: List<String>) {
        val normalizedForwardedRecipient = forwardedRecipient.trim().lowercase()
        require('@' in normalizedForwardedRecipient) { "The forwarded recipient must be an email address" }
        try {
            commands.awsLogin(profile, region)
            val loadedBootstrap = bootstrap.load()
            val configuration = loadedBootstrap.value
            val resourceNames = configuration.requiredObject("resource_names")
            val privateConfig = configuration.requiredObject("private_config")
            val tenants = privateConfig.requiredObject("email_tenants")
            val existingTenant =
                tenants.entries.singleOrNull { (_, value) ->
                    val recipient = value.jsonObject.requiredString("parse_primary_recipient")
                    recipient.equals(normalizedForwardedRecipient, ignoreCase = true)
                }
            val tenantId = existingTenant?.key ?: normalizedForwardedRecipient.sha256().take(12)
            val calendar = loadGoogleCalendar(resourceNames, privateConfig)
            val calendarId =
                existingTenant?.value?.jsonObject?.requiredString("google_calendar_id")
                    ?: calendarId(calendar, normalizedForwardedRecipient)

            setCalendarName(calendar, calendarId)
            calendarEditors.distinctBy(String::lowercase).forEach {
                share(calendar, calendarId, it)
            }

            val updatedConfiguration =
                if (existingTenant == null) {
                    addTenant(
                        configuration,
                        resourceNames,
                        privateConfig,
                        tenants,
                        tenantId,
                        normalizedForwardedRecipient,
                        calendarId
                    )
                } else {
                    configuration
                }
            if (updatedConfiguration != configuration) {
                bootstrap.update(loadedBootstrap, updatedConfiguration)
            }

            val receiver = updatedConfiguration.requiredObject("private_config").requiredObject("email_tenants")
                .getValue(tenantId).jsonObject.requiredStringList("inbound_recipients").single()
            println("Forward matching email to $receiver")
        } finally {
            aws.close()
        }

        InfrastructurePublisher(repositoryDirectory).publishWithExistingLogin()
    }

    private fun addTenant(
        configuration: JsonObject,
        resourceNames: JsonObject,
        privateConfig: JsonObject,
        tenants: JsonObject,
        tenantId: String,
        forwardedRecipient: String,
        calendarId: String
    ): JsonObject {
        check(tenantId !in tenants) { "The generated tenant ID is already in use" }
        val existingTenant = tenants.values.first().jsonObject
        val existingReceiver = existingTenant.requiredStringList("inbound_recipients").single()
        val receiver =
            existingReceiver.substringBefore('@') + "+" + tenantId + "@" + existingReceiver.substringAfter('@')
        val emailFunctions = resourceNames.requiredObject("functions").requiredObject("email_parsers")
        val receiptRules = resourceNames.requiredObject("ses_receipt_rules")
        val newTenant =
            JsonObject(
                mapOf(
                    "google_calendar_id" to JsonPrimitive(calendarId),
                    "inbound_email_prefix" to JsonPrimitive("emails/$tenantId"),
                    "inbound_recipients" to JsonArray(listOf(JsonPrimitive(receiver))),
                    "parse_primary_recipient" to JsonPrimitive(forwardedRecipient),
                    "token_key" to JsonPrimitive("tenants/$tenantId/club-locker-token.json")
                )
            )
        val updatedPrivateConfig =
            JsonObject(privateConfig + ("email_tenants" to JsonObject(tenants + (tenantId to newTenant))))
        val functionBase = emailFunctions.values.first().jsonPrimitive.content
        val receiptRuleBase = receiptRules.values.first().jsonObject.requiredString("name")
        val newReceiptRule =
            JsonObject(
                mapOf(
                    "name" to JsonPrimitive(resourceName(receiptRuleBase, tenantId)),
                    "after" to JsonPrimitive(receiptRules.values.last().jsonObject.requiredString("name"))
                )
            )
        val functions = resourceNames.requiredObject("functions")
        val functionName = resourceName(functionBase, tenantId)
        val updatedEmailFunctions = JsonObject(emailFunctions + (tenantId to JsonPrimitive(functionName)))
        val updatedFunctions =
            JsonObject(functions + ("email_parsers" to updatedEmailFunctions))
        val updatedReceiptRules = JsonObject(receiptRules + (tenantId to newReceiptRule))
        val updatedResourceNames =
            resourceNames.with("functions", updatedFunctions).with("ses_receipt_rules", updatedReceiptRules)
        return configuration.with("private_config", updatedPrivateConfig).with("resource_names", updatedResourceNames)
    }

    private suspend fun calendarId(calendar: Calendar, user: String): String {
        val marker = "squash-lambdas:${user.sha256()}"
        val existing =
            withContext(Dispatchers.IO) {
                calendar.calendarList().list().execute().items.orEmpty().filter { it.description == marker }
            }
        check(existing.size <= 1) { "More than one dedicated calendar exists for the user" }
        return existing.singleOrNull()?.id
            ?: withContext(Dispatchers.IO) {
                calendar.calendars().insert(
                    GoogleCalendar()
                        .setSummary(CALENDAR_NAME)
                        .setDescription(marker)
                        .setTimeZone("America/New_York")
                ).execute().id
            }
    }

    private suspend fun setCalendarName(calendar: Calendar, calendarId: String) =
        withContext(Dispatchers.IO) {
            val configured = calendar.calendars().get(calendarId).execute()
            if (configured.summary != CALENDAR_NAME) {
                calendar.calendars().patch(calendarId, GoogleCalendar().setSummary(CALENDAR_NAME)).execute()
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

    private suspend fun loadGoogleCalendar(resourceNames: JsonObject, privateConfig: JsonObject): Calendar {
        val credentialBytes =
            aws.readObject(
                resourceNames.requiredString("bucket"),
                privateConfig.requiredString("google_calendar_credentials_key")
            )
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

private fun JsonObject.requiredStringList(name: String): List<String> =
    getValue(name).let { value -> (value as JsonArray).map { it.jsonPrimitive.content } }

private fun JsonObject.with(name: String, value: JsonElement): JsonObject =
    JsonObject(this + (name to value))

private fun resourceName(base: String, tenantId: String): String =
    "${base.take(64 - tenantId.length - 1).trimEnd('-')}-$tenantId"

private fun String.sha256(): String =
    MessageDigest.getInstance("SHA-256").digest(toByteArray()).joinToString("") { "%02x".format(it) }

suspend fun main(args: Array<String>) =
    ProvisionUser().main(args)
