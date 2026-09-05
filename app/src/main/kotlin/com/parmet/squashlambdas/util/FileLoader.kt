package com.parmet.squashlambdas.util

import com.parmet.squashlambdas.FileConfig
import com.parmet.squashlambdas.aws.ObjectStorage
import dev.zacsweers.metro.Inject
import java.io.File

@Inject
class FileLoader(
    private val objectStorage: ObjectStorage
) {
    suspend fun streamFile(config: FileConfig) =
        when (config.location) {
            "s3" -> objectStorage.read(requireNotNull(config.bucket), requireNotNull(config.key)).inputStream()

            "local" ->
                File(config.fileName!!).inputStream()

            else -> error("unsupported location")
        }
}
