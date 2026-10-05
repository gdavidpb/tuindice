package com.gdavidpb.tuindice.data.source.sync.api.response

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object SyncReportStatusResponseSerializer : KSerializer<SyncReportStatusResponse> {
	override val descriptor: SerialDescriptor =
		PrimitiveSerialDescriptor("SyncReportStatusResponse", PrimitiveKind.STRING)

	override fun deserialize(decoder: Decoder): SyncReportStatusResponse {
		val raw = decoder.decodeString()

		return SyncReportStatusResponse.entries.firstOrNull { status -> status.wireName == raw }
			?: SyncReportStatusResponse.Unknown
	}

	override fun serialize(encoder: Encoder, value: SyncReportStatusResponse) {
		encoder.encodeString(value.wireName ?: "unknown")
	}
}
