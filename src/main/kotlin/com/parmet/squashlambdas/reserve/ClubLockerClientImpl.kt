package com.parmet.squashlambdas.reserve

import com.google.common.base.Joiner
import com.google.common.base.Preconditions.checkState
import com.google.common.net.HttpHeaders
import com.google.common.net.MediaType.FORM_DATA
import com.google.common.net.MediaType.JSON_UTF_8
import com.google.common.net.UrlEscapers
import com.google.common.util.concurrent.AbstractIdleService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.parmet.squashlambdas.BOSTON
import com.parmet.squashlambdas.activity.Court
import com.parmet.squashlambdas.activity.Court.Court1
import com.parmet.squashlambdas.activity.Court.Court2
import com.parmet.squashlambdas.activity.Court.Court3
import com.parmet.squashlambdas.activity.Court.Court5
import com.parmet.squashlambdas.activity.Court.Court6
import com.parmet.squashlambdas.activity.Court.Court7
import com.parmet.squashlambdas.activity.Court.RacquetsCourt
import com.parmet.squashlambdas.activity.Court.TennisCourt
import com.parmet.squashlambdas.activity.Match
import com.parmet.squashlambdas.fromJson
import mu.KotlinLogging
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.lang.reflect.Type
import java.time.LocalDate

internal class ClubLockerClientImpl(
    private val username: String,
    private val password: String
) : ClubLockerClient, AbstractIdleService() {

    private val logger = KotlinLogging.logger { }

    private val httpClient = OkHttpClient.Builder().followRedirects(false).build()
    private val baseUrl = "https://api.ussquash.com"

    private val gson = Gson()

    private val tennisAndRacquetClubId = 1413
    private val resource = "$baseUrl/resources/res"
    private val clubResource = "$resource/clubs/$tennisAndRacquetClubId"

    private lateinit var accessToken: String

    private lateinit var directoryService: DirectoryService

    override fun startUp() {
        accessToken = authenticate()
        directoryService = DirectoryService(this)
    }

    private fun authenticate() =
        responseBody(
            Request.Builder()
                .url("$baseUrl/clublocker_login")
                .post(
                    RequestBody.create(
                        MediaType.get(FORM_DATA.toString()),
                        Joiner.on("&").withKeyValueSeparator("=").join(
                            mapOf(
                                "username" to username,
                                "password" to password
                            ).mapValues { (_, v) ->
                                UrlEscapers.urlFormParameterEscaper().escape(v)
                            }
                        ))))
            .substringAfter("access_token=")

    override fun user(): UserResp =
        execute("$resource/user")

    override fun courts(): List<CourtResp> =
        execute("$clubResource/courts")

    override fun directory(): List<User> =
        execute("$clubResource/players/directory")

    override fun slotsTaken(from: LocalDate, to: LocalDate): List<TakenSlot> =
        execute("$clubResource/slots_taken/from/$from/to/$to")

    private fun responseBody(builder: Request.Builder): String {
        val response = response(builder)
        val respBody = response.body()!!.string()
        logger.info { "Received response: ${response.code()}, $respBody" }
        return respBody
    }

    private fun response(builder: Request.Builder): Response {
        val request = builder.build()
        logger.info { "Performing request: $request, body: ${request.body()}" }
        return httpClient.newCall(request).execute()
    }

    private inline fun <reified T> execute(resource: String): T {
        checkRunning()
        return gson.fromJson(responseBody(Request.Builder().url(resource).authorized()))
    }

    override fun makeReservation(match: Match): ReservationResp {
        checkRunning()
        val response =
            response(
                Request.Builder()
                    .url("$clubResource/reservations")
                    .authorized()
                    .post(
                        RequestBody.create(
                            MediaType.get(JSON_UTF_8.toString()),
                            match.toReservationRequest().toJson())))

        val code = response.code()
        val body: Map<String, Any> = gson.fromJson(response.body()!!.string())
        return if (code == 200) {
            if (body.containsKey("createDenied")) {
                ReservationResp.Error(code, body["reason"] as String, match)
            } else {
                checkState(body.containsKey("id"), "Deduced success but body contained no id: %s", body)
                ReservationResp.Success((body["id"] as Number).toInt())
            }
        } else {
            ReservationResp.Error(code, body["error"] as String, match)
        }
    }

    private fun Match.toReservationRequest(): ReservationReq {
        return ReservationReq(
            tennisAndRacquetClubId,
            court.clubLockerId,
            start.atZone(BOSTON).toLocalDate(),
            Slot(start.atZone(BOSTON).toLocalTime(), end.atZone(BOSTON).toLocalTime()),
            players.map {
                val id = directoryService.idForPlayer(it)
                if (id != null) {
                    logger.info { "Found id $id for $it" }
                    Player.member(id)
                } else {
                    Player.guest(it.name!!)
                }
            }
        )
    }

    private val Court.clubLockerId: Int
        get() = when (this) {
            Court1 -> 1411
            Court2 -> 1688
            Court3 -> 1689
            Court5 -> 1692
            Court6 -> 1693
            Court7 -> 1694
            TennisCourt -> 1690
            RacquetsCourt -> 1691
        }

    private fun Request.Builder.authorized() =
        header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            .header(HttpHeaders.ACCEPT, JSON_UTF_8.toString())

    private fun checkRunning() = checkState(isRunning)

    override fun shutDown() {}
}

@Suppress("UNUSED")
internal data class ReservationReq(
    val clubId: Int,
    val courtId: Int,
    @Transient
    val localDate: LocalDate,
    @Transient
    val timeSlot: Slot,
    val players: List<Player>
) {
    private val date = localDate.toString()
    private val startTime = timeSlot.startTime
    private val endTime = timeSlot.endTime
    private val slot = timeSlot.slot
    private val type = "match"
    private val numberOfPlayers = 2
    private val isPrivate = false
    private val restrictJoinByRating = false
    private val minimumRating = -1
    private val maximumRating = 1.1
    private val notes = listOf<Any>()
    private val matchType = "singles"
    private val customMatchType = 144
    private val applyUserRestrictionsForAdmin = false
    private val paymentCard = null
    private val payingForAll = false

    fun toJson() = GSON.toJson(this)

    companion object {
        private val GSON =
            GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(Player::class.java, Player.SERIALIZER)
                .create()
    }
}

internal class Player
private constructor(
    val type: String,
    val id: Any?,
    @Suppress("UNUSED")
    val guestName: String?
) {
    companion object {
        fun member(id: Any) = Player("member", id, null)

        fun guest(name: String) = Player("guest", null, name)

        internal val SERIALIZER = object : JsonSerializer<Player> {
            // Don't serialize nulls
            private val gson = Gson()

            override fun serialize(src: Player, typeOfSrc: Type, context: JsonSerializationContext) =
                gson.toJsonTree(src)
        }
    }
}
