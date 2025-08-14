package com.parmet.squashlambdas.activity

import com.parmet.squashlambdas.activity.Sport.Fitness
import com.parmet.squashlambdas.activity.Sport.Hardball
import com.parmet.squashlambdas.activity.Sport.Racquets
import com.parmet.squashlambdas.activity.Sport.Squash
import com.parmet.squashlambdas.activity.Sport.Tennis
import java.util.regex.Pattern

sealed class Court(
    val sport: Sport
) {
    abstract val pretty: String

    override fun toString() =
        pretty

    object Court1 : Court(Squash) {
        override val pretty = "Court 1"
    }

    object Court2 : Court(Squash) {
        override val pretty = "Court 2"
    }

    object Court3 : Court(Squash) {
        override val pretty = "Court 3"
    }

    object Court5 : Court(Hardball) {
        override val pretty = "Court 5"
    }

    object Court6 : Court(Hardball) {
        override val pretty = "Court 6"
    }

    object Court7 : Court(Hardball) {
        override val pretty = "Court 7"
    }

    object TennisCourt : Court(Tennis) {
        override val pretty = "Tennis Court"
    }

    object RacquetsCourt : Court(Racquets) {
        override val pretty = "Racquets Court"
    }

    object FitnessClasses : Court(Fitness) {
        override val pretty = "Fitness Classes"
    }

    companion object
}

// "Court: Court #x" (match creation)
// "Courts: Court #x" (activity creation)
private val NUMBERED_COURT = Pattern.compile(".*Court #(\\d) [/\\-] (Squash|Hardball).*")

// "Court: Court Tennis - Court Tennis" (match creation)
// "Court: Court Tennis / Court Tennis" (player joins)
private val TENNIS_COURT = Pattern.compile(".*Court Tennis [-/] Court Tennis.*")

// "Court: Racquets - Racquets" (match creation)
// "Court: Racquets / Racquets" (match deletion)
private val RACQUETS_COURT = Pattern.compile(".*Racquets [-/] Racquets.*")

private val map =
    Court::class.nestedClasses
        .map { it.objectInstance }
        .filterIsInstance<Court>()
        .associateBy { it.pretty }

internal fun Court.Companion.valueOf(value: String) =
    requireNotNull(map[value]) {
        "No enum constant ${Court::class.java.name}.$value"
    }

internal fun Court.Companion.fromLocationString(body: String) =
    when {
        TENNIS_COURT.matcher(body).matches() ->
            Court.TennisCourt
        RACQUETS_COURT.matcher(body).matches() ->
            Court.RacquetsCourt
        else -> {
            val matcher = NUMBERED_COURT.matcher(body)
            require(matcher.matches()) { "Unable to parse court from $body" }
            valueOf("Court ${matcher.group(1)}")
        }
    }
