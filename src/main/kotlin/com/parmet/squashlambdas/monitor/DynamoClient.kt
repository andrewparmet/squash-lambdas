package com.parmet.squashlambdas.monitor

import com.parmet.squashlambdas.clublocker.Slot
import java.time.LocalDate

internal interface DynamoClient {
    fun save(date: LocalDate, slots: List<Slot>)

    fun loadLatest(date: LocalDate): List<Slot>
}
