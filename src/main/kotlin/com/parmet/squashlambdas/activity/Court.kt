package com.parmet.squashlambdas.activity

import com.google.common.base.Preconditions.checkArgument
import com.parmet.squashlambdas.activity.Sport.Hardball
import com.parmet.squashlambdas.activity.Sport.Racquets
import com.parmet.squashlambdas.activity.Sport.Squash
import com.parmet.squashlambdas.activity.Sport.Tennis
import java.util.regex.Pattern

internal sealed class Court(val sport: Sport) {
    abstract val pretty: String

    override fun toString() = pretty

    object Court1 : CourtN(Squash, 1)
    object Court2 : CourtN(Squash, 2)
    object Court3 : CourtN(Squash, 3)

    object Court5 : CourtN(Hardball, 5)
    object Court6 : CourtN(Hardball, 6)
    object Court7 : CourtN(Hardball, 7)

    object TennisCourt : Court(Tennis) {
        override val pretty = "Tennis Court"
    }

    object RacquetsCourt : Court(Racquets) {
        override val pretty = "Racquets Court"
    }

    companion object
}

// "Court: Court #x" (match creation)
// "Courts: Court #x" (activity creation)
private val NUMBERED_COURT = Pattern.compile(".*Courts?: Court #(\\d) [/\\-].*")

private val map =
    Court::class.nestedClasses
        .map { it.objectInstance }
        .filterIsInstance<Court>()
        .associateBy { it.pretty }

private fun valueOf(value: String) = requireNotNull(map[value]) {
    "No enum constant ${Court::class.java.name}.$value"
}

internal fun Court.Companion.fromLocationString(body: String): Court {
    val matcher = NUMBERED_COURT.matcher(body)
    checkArgument(matcher.matches(), "Unable to parse court from %s", body)
    return valueOf("Court ${matcher.group(1)}")
}

internal open class CourtN(sport: Sport, n: Int) : Court(sport) {
    override val pretty = "Court $n"
}
