package com.parmet.squashlambdas.email

import biweekly.Biweekly
import com.sun.mail.util.BASE64DecoderStream
import org.jsoup.Jsoup
import java.nio.charset.StandardCharsets.UTF_8
import javax.mail.BodyPart
import javax.mail.Part

internal class MimeParser<T> private constructor(
    private val mimeType: String,
    private val filter: (String) -> Boolean = { true },
    private val parser: (Part) -> Appendable<T>
) {
    fun isFor(part: Part) =
        part.isMimeType(mimeType) && filter(part.fileName)

    fun parse(part: Part): Appendable<T> {
        require(isFor(part)) {
            "Cannot parse part with type ${part.contentType} using parser for type $mimeType"
        }
        return parser(part)
    }

    companion object {
        private val EXTRACT_BASE_64 =
            { bodyPart: Part ->
                AppendableString((bodyPart.content as BASE64DecoderStream).bufferedReader().readText())
            }

        private val ENDS_WITH_CSV =
            { fileName: String -> fileName.endsWith(".csv") }

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

        val TEXT_CSV =
            MimeParser("text/csv", { true }, EXTRACT_BASE_64)

        val APPLICATION_OCTET_STREAM_CSV =
            MimeParser(
                "application/octet-stream",
                ENDS_WITH_CSV,
                EXTRACT_BASE_64
            )

        val APPLICATION_VND_MS_EXCEL =
            MimeParser(
                "application/vnd.ms-excel",
                ENDS_WITH_CSV,
                EXTRACT_BASE_64
            )
    }
}
