package com.parmet.squashlambdas.s3

import com.fatboyindustrial.gsonjavatime.InstantConverter
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import java.time.Instant

internal data class S3EmailNotification(
    private val eventVersion: String,
    private val eventSource: String,
    private val awsRegion: String,
    private val eventTime: Instant,
    private val eventName: String,
    @SerializedName("s3")
    val s3ObjectInfo: S3CreateObjectInfo
) {
    companion object {
        private val GSON =
            GsonBuilder()
                .registerTypeAdapter(Instant::class.java, InstantConverter())
                .create()

        fun fromInputObject(input: Any) =
            GSON.fromJson(
                GSON.toJsonTree(input).asJsonObject.getAsJsonArray("Records").get(0),
                S3EmailNotification::class.java
            )
    }
}
