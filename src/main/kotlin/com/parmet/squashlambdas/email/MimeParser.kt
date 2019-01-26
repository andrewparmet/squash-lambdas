package com.parmet.squashlambdas.email

import biweekly.Biweekly
import com.google.common.base.Preconditions.checkArgument
import org.jsoup.Jsoup
import java.nio.charset.StandardCharsets.UTF_8
import javax.mail.Part

class MimeParser<T>
private constructor(
    private val mimeType: String,
    private val parser: (Part) -> Appendable<T>
) {
    fun isFor(part: Part) = part.isMimeType(mimeType)

    fun parse(part: Part): Appendable<T> {
        checkArgument(
            isFor(part),
            "Cannot parse part with type %s using parser for type %s",
            part.contentType,
            mimeType
        )
        return parser(part)
    }

    companion object {
        val TEXT_PLAIN =
            MimeParser("text/plain") { bodyPart ->
                AppendableString(StringBuilder(bodyPart.content as String))
            }

        val TEXT_HTML =
            MimeParser("text/html") { bodyPart ->
                AppendableString(StringBuilder(Jsoup.parse(bodyPart.content as String).text()))
            }

        val TEXT_CALENDAR =
            MimeParser("text/calendar") { bodyPart ->
                bodyPart.inputStream.use { AppendableList(Biweekly.parse(it.reader(UTF_8).readText()).all()) }
            }
    }
}
