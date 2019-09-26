package com.parmet.squashlambdas.clublocker

import com.google.common.base.Joiner
import com.google.common.collect.ImmutableBiMap
import com.google.common.net.HttpHeaders.ACCEPT
import com.google.common.net.HttpHeaders.AUTHORIZATION
import com.google.common.net.MediaType.FORM_DATA
import com.google.common.net.MediaType.JSON_UTF_8
import com.google.common.net.UrlEscapers
import com.google.common.util.concurrent.AbstractIdleService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
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
import com.parmet.squashlambdas.inBoston
import com.parmet.squashlambdas.reserve.endTime
import com.parmet.squashlambdas.reserve.slot
import com.parmet.squashlambdas.reserve.startTime
import mu.KotlinLogging
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okio.Buffer
import org.apache.http.client.HttpResponseException
import org.jsoup.Jsoup
import java.lang.reflect.Type
import java.time.LocalDate

internal class ClubLockerClientImpl(
    private val user: ClubLockerUser
) : ClubLockerClient, AbstractIdleService() {

    private val logger = KotlinLogging.logger { }

    private val httpClient = OkHttpClient.Builder().followRedirects(false).build()
    private val gson = Gson()

    private val baseUrl = "https://api.ussquash.com"
    private val tennisAndRacquetClubId = 1413
    private val resource = "$baseUrl/resources/res"
    private val clubResource = "$resource/clubs/$tennisAndRacquetClubId"

    private lateinit var accessToken: String
    private lateinit var directory: Directory

    override fun startUp() {
        accessToken = authenticate()
        directory = Directory(this)
    }

    private fun authenticate() =
        responseBody(
            Request.Builder()
                .url("$baseUrl/clublocker_login")
                .post(
                    Joiner.on("&").withKeyValueSeparator("=").join(
                        mapOf(
                            "username" to user.username,
                            "password" to user.password
                        ).mapValues { (_, v) ->
                            UrlEscapers.urlFormParameterEscaper().escape(v)
                        }
                    ).toRequestBody(FORM_DATA.toString().toMediaType())
                )
        ).substringAfter("access_token=")

    override fun user(): UserResp =
        get("$resource/user")

    override fun courts(): List<CourtResp> =
        get("$clubResource/courts")

    override fun directory(): List<User> =
        get("$clubResource/players/directory")

    override fun slotsTaken(from: LocalDate, to: LocalDate): List<Slot> =
        get("$clubResource/slots_taken/from/$from/to/$to")

    private fun responseBody(builder: Request.Builder): String {
        val response = response(builder)
        val code = response.code
        val respBody = response.body!!.string()
        logger.info { "Received response: $code, $respBody" }
        if (code >= 400) {
            try {
                throw HttpResponseException(code, Jsoup.parse(respBody).wholeText().replace("\n", ";"))
            } catch (ex: Exception) {
                logger.info(ex) { "Error while parsing response error body" }
                throw HttpResponseException(code, respBody.replace("\n", ";"))
            }
        }
        return respBody
    }

    private fun response(builder: Request.Builder): Response {
        val request = builder.build()
        logger.info { "Performing request: $request, body: ${Buffer().also { request.body?.writeTo(it) }}" }
        return httpClient.newCall(request).execute()
    }

    private inline fun <reified T> get(resource: String): T {
        checkRunning()
        return gson.fromJson(responseBody(Request.Builder().url(resource).authorized()))
    }

    override fun makeReservation(match: Match): ReservationResp {
        try {
            checkRunning()
            val response =
                response(
                    Request.Builder()
                        .url("$clubResource/reservations")
                        .authorized()
                        .post(
                            match.toReservationRequest().toJson()
                                .toRequestBody(JSON_UTF_8.toString().toMediaType())
                        )
                )

            val code = response.code
            val body: Map<String, Any> = gson.fromJson(response.body!!.string())
            return if (code == 200) {
                if (body.containsKey("createDenied")) {
                    ReservationResp.Error(code, body["reason"] as String, match)
                } else {
                    check(body.containsKey("id")) { "Deduced success but body contained no id: $body" }
                    ReservationResp.Success((body["id"] as Number).toInt(), match)
                }
            } else {
                ReservationResp.Error(code, body["error"] as String, match)
            }
        } catch (t: Throwable) {
            return ReservationResp.Failure(t, match)
        }
    }

    private fun Match.toReservationRequest(): ReservationReq {
        return ReservationReq(
            tennisAndRacquetClubId,
            court.clubLockerId,
            start.inBoston().toLocalDate(),
            com.parmet.squashlambdas.reserve.Slot(
                start.inBoston().toLocalTime(),
                end.inBoston().toLocalTime()
            ),
            players.map {
                val id = directory.idForPlayer(it)
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
        get() = COURTS_BY_ID.inverse().getValue(this)

    private fun Request.Builder.authorized() =
        header(AUTHORIZATION, "Bearer $accessToken")
            .header(ACCEPT, JSON_UTF_8.toString())

    private fun checkRunning() =
        check(isRunning)

    override fun shutDown() = Unit
}

internal data class ClubLockerUser(
    val username: String,
    val password: String,
    val name: String
)

internal val COURTS_BY_ID =
    ImmutableBiMap.builder<Int, Court>()
        .put(1411, Court1)
        .put(1688, Court2)
        .put(1689, Court3)
        .put(1692, Court5)
        .put(1693, Court6)
        .put(1694, Court7)
        .put(1690, TennisCourt)
        .put(1691, RacquetsCourt)
        .build()

@Suppress("UNUSED")
internal data class ReservationReq(
    val clubId: Int,
    val courtId: Int,
    @Transient
    val localDate: LocalDate,
    @Transient
    val timeSlot: com.parmet.squashlambdas.reserve.Slot,
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
