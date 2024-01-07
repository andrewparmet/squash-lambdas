package com.parmet.squashlambdas.email

internal object BodyExtractor : AbstractMimeMessageExtractor<StringBuilder>(::AppendableString) {
    override fun parsers() =
        listOf(MimeParser.TEXT_PLAIN, MimeParser.TEXT_HTML)
}
