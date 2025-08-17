package com.parmet.squashlambdas.reserve

import java.io.InputStream

internal fun <T> InputStream.mapNonEmptyLines(action: (String) -> T) =
    reader().useLines { lines ->
        lines.filter { it.isNotBlank() }
            .map { it.trim() }
            .map(action)
            .toList()
    }
