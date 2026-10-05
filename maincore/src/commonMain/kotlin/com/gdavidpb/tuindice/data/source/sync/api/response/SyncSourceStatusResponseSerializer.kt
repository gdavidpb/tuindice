package com.gdavidpb.tuindice.data.source.sync.api.response

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object SyncSourceStatusResponseSerializer : KSerializer<SyncSourceStatusResponse> {
	override val descriptor: SerialDescriptor =
		PrimitiveSerialDescriptor("SyncSourceStatusResponse", PrimitiveKind.STRING)

	override fun deserialize(decoder: Decoder): SyncSourceStatusResponse {
		val raw = decoder.decodeString()

		return SyncSourceStatusResponse.entries.firstOrNull { status -> status.wireName == raw }
			?: SyncSourceStatusResponse.Unknown
	}

	override fun serialize(encoder: Encoder, value: SyncSourceStatusResponse) {
		encoder.encodeString(value.wireName ?: "unknown")
	}
}
