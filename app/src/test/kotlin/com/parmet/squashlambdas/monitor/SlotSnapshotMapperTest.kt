package com.parmet.squashlambdas.monitor

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import com.google.common.truth.Truth.assertThat
import com.parmet.squashlambdas.monitor.dynamodbmapper.generatedschemas.SlotSnapshotConverter
import org.junit.jupiter.api.Test

class SlotSnapshotMapperTest {
    @Test
    fun `schema preserves the existing attribute types`() {
        val item = SlotSnapshotConverter.convertRight(SlotSnapshot("2026-09-05/taken", listOf("slot"), "now", 123))

        assertThat(item["filename"]).isEqualTo(AttributeValue.S("2026-09-05/taken"))
        assertThat(item["entries"]).isEqualTo(AttributeValue.L(listOf(AttributeValue.S("slot"))))
        assertThat(item["modifiedTime"]).isEqualTo(AttributeValue.S("now"))
        assertThat(item["ttl"]).isEqualTo(AttributeValue.N("123"))
    }
}
