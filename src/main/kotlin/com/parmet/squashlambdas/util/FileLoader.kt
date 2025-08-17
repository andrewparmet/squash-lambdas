package com.parmet.squashlambdas.util

import com.parmet.squashlambdas.FileConfig
import software.amazon.awssdk.services.s3.S3Client
import java.io.File
import javax.inject.Inject

class FileLoader @Inject constructor(
    private val s3Client: S3Client
) {
    fun streamFile(config: FileConfig) =
        when (config.location) {
            "s3" ->
                s3Client.getObject {
                    it.bucket(config.bucket)
                    it.key(config.key)
                }

            "local" ->
                File(config.fileName!!).inputStream()

            else -> error("unsupported location")
        }
}
