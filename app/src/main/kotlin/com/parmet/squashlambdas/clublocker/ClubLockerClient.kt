package com.parmet.squashlambdas.clublocker

import com.parmet.squashlambdas.activity.Match
import kotlinx.serialization.Serializable
import java.time.LocalDate

interface ClubLockerClient {
    fun init()

    suspend fun user(): UserResp

    suspend fun courts(): List<CourtResp>

    suspend fun slotsTaken(from: LocalDate, to: LocalDate): List<Slot>

    suspend fun reservation(id: Int): Reservation

    suspend fun makeReservation(match: Match): ReservationResp

    suspend fun directory(): List<User>
}

@Serializable
data class UserResp(
    val id: Int,
    val affiliations: List<Affiliation>,
    val email: String
)

@Serializable
data class Affiliation(
    val id: Int,
    val name: String
)

@Serializable
data class CourtResp(
    val id: Int,
    val name: String,
    val slotLengthMinutes: Int
)

@Serializable
data class Slot(
    val id: Int,
    val reservationId: Int,
    val court: Int,
    val startTime: Int,
    val endTime: Int,
    val startUtc: Long,
    val type: String
)

@Serializable
data class Reservation(
    val players: List<ReservationPlayer>
)

@Serializable
data class ReservationPlayer(
    val text: String,
    val isMyself: Boolean
)

sealed class ReservationResp {
    internal data class Success(
        val id: Int,
        val match: Match
    ) : ReservationResp()

    sealed class NonSuccess : ReservationResp()

    internal data class Error(
        val statusCode: Int,
        val message: String?,
        val match: Match
    ) : NonSuccess()

    internal data class Failure(
        val t: Throwable,
        val match: Match
    ) : NonSuccess()
}

@Serializable
data class User(
    val id: Int,
    private val name: String
) {
    val fullName
        get() = name.split(", ").reversed().joinToString(" ")
}
