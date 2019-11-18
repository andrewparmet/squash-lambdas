package com.parmet.squashlambdas.matchfind

import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import java.io.StringWriter

private const val RATING = "rating"
private const val FIRST_NAME = "firstName"
private const val LAST_NAME = "lastName"
private const val EMAIL = "email"
private const val TENNIS_HCAP = "rtoSinglesHcap"

enum class CsvType(private val columnName: String) {
    SQUASH(RATING),
    TENNIS(TENNIS_HCAP);

    fun filterCsv(csv: String): String {
        val reader = CSVReader(csv.reader())
        val header = reader.readNext().toList()

        val ratingIdx = header.indexOf(columnName)
        val firstNameIdx = header.indexOf(FIRST_NAME)
        val lastNameIdx = header.indexOf(LAST_NAME)
        val emailIdx = header.indexOf(EMAIL)

        val rawCsv = reader.readAll().map { it.toList() }

        val outHeader = listOf(columnName, FIRST_NAME, LAST_NAME, EMAIL)
        val outRows = rowsFromColumnIdx(rawCsv, ratingIdx, firstNameIdx, lastNameIdx, emailIdx)

        val writer = StringWriter()
        CSVWriter(writer).writeAll((listOf(outHeader) + outRows).map { it.toTypedArray() })
        return writer.toString()
    }

    private fun rowsFromColumnIdx(rawCsv: List<List<String>>, ratingIdx: Int, firstNameIdx: Int, lastNameIdx: Int, emailIdx: Int) =
        rawCsv.filter {
            it.getOrElse(ratingIdx) { "" }.isNotEmpty()
        }.map {
            listOf(it[ratingIdx], it[firstNameIdx], it[lastNameIdx], it[emailIdx])
        }.sortedByDescending { it[0].toDouble() }
}
