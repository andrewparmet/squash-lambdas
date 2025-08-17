package com.parmet.squashlambdas.email

import org.jsoup.Jsoup
import javax.mail.Part

internal class MimeParser<T> private constructor(
    private val mimeType: String,
    private val filter: (String) -> Boolean = { true },
    private val parser: (Part) -> Appendable<T>
) {
    fun isFor(part: Part) =
        part.isMimeType(mimeType) && part.fileName.let { it == null || filter(it) }

    fun parse(part: Part): Appendable<T> {
        require(isFor(part)) {
            "Cannot parse part with type ${part.contentType} using parser for type $mimeType"
        }
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
    }
}
