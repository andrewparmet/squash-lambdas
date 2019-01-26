package com.parmet.squashlambdas.email

import javax.mail.MessagingException
import javax.mail.Part
import javax.mail.internet.ContentType
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

abstract class AbstractMimeMessageExtractor<T>(
    private val newInstance: () -> Appendable<T>
) {
    fun extract(message: MimeMessage): Appendable<T> {
        return if (message.isMimeType("multipart/*")) {
            getFromMimeMultipart(message.content as MimeMultipart)
        } else {
            getFromPart(message)
        }
    }

    private fun getFromMimeMultipart(mimeMultipart: MimeMultipart): Appendable<T> {
        val count = mimeMultipart.count
        if (count == 0) {
            throw MessagingException("Multipart with no body parts not supported.")
        }
        if (ContentType(mimeMultipart.contentType).match("multipart/alternative")) {
            // Alternatives appear in order of increasing faithfulness to the original content.
            return getFromPart(mimeMultipart.getBodyPart(count - 1))
        }
        val t = newInstance()
        for (i in 0..(count - 1)) {
            t.appendAll(getFromPart(mimeMultipart.getBodyPart(i)))
        }
        return t
    }

    private fun getFromPart(part: Part): Appendable<T> {
        parsers().forEach {
            if (it.isFor(part)) {
                return it.parse(part)
            }
        }
        return newInstance()
    }

    /** Parsers specified in descending preferred parse order (most preferred first). */
    protected abstract fun parsers(): Iterable<MimeParser<T>>
}
