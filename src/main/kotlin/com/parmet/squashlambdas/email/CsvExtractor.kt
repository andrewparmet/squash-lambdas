package com.parmet.squashlambdas.email

internal object CsvExtractor : AbstractMimeMessageExtractor<StringBuilder>(::AppendableString) {
    override fun parsers() =
        listOf(
            MimeParser.TEXT_CSV,
            MimeParser.APPLICATION_OCTET_STREAM_CSV,
            MimeParser.APPLICATION_VND_MS_EXCEL,
        )
}
