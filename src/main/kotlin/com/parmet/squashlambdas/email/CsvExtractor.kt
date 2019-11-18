package com.parmet.squashlambdas.email

internal object CsvExtractor : AbstractMimeMessageExtractor<StringBuilder>(::AppendableString) {
    override fun parsers() =
        listOf(MimeParser.TEXT_CSV)
}
