package com.parmet.squashlambdas.activity

import com.parmet.squashlambdas.activity.Sport.Fitness
import com.parmet.squashlambdas.activity.Sport.Hardball
import com.parmet.squashlambdas.activity.Sport.Racquets
import com.parmet.squashlambdas.activity.Sport.Squash
import com.parmet.squashlambdas.activity.Sport.Tennis
import com.parmet.squashlambdas.notify.CourtSerializer
import kotlinx.serialization.Serializable
import java.util.regex.Pattern

@Serializable(with = CourtSerializer::class)
enum class Court(
    val sport: Sport,
    val pretty: String
) {
    Court1(Squash, "Court 1"),
    Court2(Squash, "Court 2"),
    Court3(Squash, "Court 3"),
    Court5(Hardball, "Court 5"),
    Court6(Hardball, "Court 6"),
    Court7(Hardball, "Court 7"),
    TennisCourt(Tennis, "Tennis Court"),
    RacquetsCourt(Racquets, "Racquets Court"),
    FitnessClasses(Fitness, "Fitness Classes");

    override fun toString() =
        pretty

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

private val courtsByPretty = Court.entries.associateBy { it.pretty }

internal fun Court.Companion.fromPrettyName(value: String) =
    requireNotNull(courtsByPretty[value]) {
        "No court named $value"
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
            fromPrettyName("Court ${matcher.group(1)}")
        }
    }
