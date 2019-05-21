package com.parmet.squashlambdas.reserve

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.parmet.squashlambdas.clublocker.Affiliation
import com.parmet.squashlambdas.clublocker.CourtResp
import com.parmet.squashlambdas.clublocker.Player
import com.parmet.squashlambdas.clublocker.ReservationReq
import com.parmet.squashlambdas.clublocker.Slot
import com.parmet.squashlambdas.clublocker.User
import com.parmet.squashlambdas.clublocker.UserResp
import com.parmet.squashlambdas.testutil.PARSER
import com.parmet.squashlambdas.testutil.getResourceAsString
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class SerializationTests {
    private val gson = Gson()

    @Test
    fun `test court parsing`() {
        val courts: List<CourtResp> =
            gson.fromJson(getResourceAsString("courts.json"), object : TypeToken<List<CourtResp>>() {}.type)

        assertThat(courts).containsExactly(
                CourtResp(1690, "Court Tennis", 60),
                CourtResp(1691, "Racquets", 60),
                CourtResp(1411, "Court #1", 45),
                CourtResp(1688, "Court #2", 45),
                CourtResp(1689, "Court #3", 45),
                CourtResp(1692, "Court #5", 45),
                CourtResp(1693, "Court #6", 45),
                CourtResp(1694, "Court #7", 45)
        )
    }

    @Test
    fun `test reservation request serialization`() {
        val req =
                ReservationReq(
                        1413,
                        1692,
                        LocalDate.parse("2019-02-02"),
                        Slot(LocalTime.parse("18:45"), LocalTime.parse("19:30")),
                        listOf(Player.member(167759), Player.member("open"))
                )

        assertThat(PARSER.parse(req.toJson()).asJsonObject.entrySet())
            .containsExactlyElementsIn(
                PARSER.parse(getResourceAsString("reservation-request.json")).asJsonObject.entrySet())
    }

    @Test
    fun `test slots taken parsing`() {
        val taken: List<com.parmet.squashlambdas.clublocker.Slot> =
            gson.fromJson(getResourceAsString("slots-taken.json"), object : TypeToken<List<com.parmet.squashlambdas.clublocker.Slot>>() {}.type)

        assertThat(taken.subList(0, 2)).containsExactly(
            Slot(
                535787,
                412844,
                1411,
                1930,
                2015,
                1495323000
            ),
            Slot(
                535800,
                412844,
                1411,
                2015,
                2100,
                1495325700
            )
        )
    }

    @Test
    fun `test user parsing`() {
        assertThat(gson.fromJson(getResourceAsString("user.json"), UserResp::class.java))
            .isEqualTo(
                    UserResp(
                            167759,
                            listOf(Affiliation(1413, "Tennis & Racquet Club")),
                            "joecool@peanuts.com")
            )
    }

    @Test
    fun `test directory parsing`() {
        val directory: List<User> =
            gson.fromJson(getResourceAsString("directory.json"), object : TypeToken<List<User>>() {}.type)

        assertThat(directory).containsExactly(
                User(167759, "Parmet, Andrew", "joecool@peanuts.com")
        )
    }
}
