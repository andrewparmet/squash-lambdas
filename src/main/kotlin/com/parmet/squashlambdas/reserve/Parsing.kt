package com.parmet.squashlambdas.reserve

internal fun <T> String.mapNonEmptyLines(action: (String) -> T) =
    lines()
        .filter { it.isNotBlank() }
        .map { it.trim() }
        .map(action)
