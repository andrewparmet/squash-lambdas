package com.parmet.squashlambdas.matchfind

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.amazonaws.services.simpleemail.model.RawMessage
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest
import com.google.common.net.MediaType
import com.parmet.squashlambdas.util.inBoston
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.time.Instant
import java.util.Properties
import javax.activation.DataHandler
import javax.mail.Message
import javax.mail.Session
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.mail.util.ByteArrayDataSource

class CsvEmailSender(
    private val sesClient: AmazonSimpleEmailService
) {
    fun send(squashCsv: String, tennisCsv: String, recipient: String): Any {
        val message =
            MimeMessage(Session.getDefaultInstance(Properties())).apply {
                subject = "MatchFind: ${Instant.now().inBoston()}"
                setFrom("matchfind@andrew.parmet.com")
                setRecipients(Message.RecipientType.TO, recipient)
                setContent(
                    MimeMultipart("mixed").apply {
                        addBodyPart(
                            MimeBodyPart().apply {
                                setContent(
                                    MimeMultipart("alternative").apply {
                                        addBodyPart(
                                            MimeBodyPart().apply {
                                                setContent(
                                                    "See attached Squash.csv and Tennis.csv",
                                                    MediaType.PLAIN_TEXT_UTF_8.toString()
                                                )
                                            }
                                        )
                                    }
                                )
                            }
                        )
                        addBodyPart(
                            MimeBodyPart().apply {
                                dataHandler = DataHandler(ByteArrayDataSource(squashCsv.toByteArray(), "text/csv"))
                                fileName = "Squash.csv"
                            }
                        )
                        addBodyPart(
                            MimeBodyPart().apply {
                                dataHandler = DataHandler(ByteArrayDataSource(tennisCsv.toByteArray(), "text/csv"))
                                fileName = "Tennis.csv"
                            }
                        )
                    }
                )
            }

        return sesClient.sendRawEmail(
            SendRawEmailRequest(
                RawMessage(
                    ByteBuffer.wrap(
                        ByteArrayOutputStream().also { message.writeTo(it) }
                            .toByteArray()
                    )
                )
            )
        )
    }
}
