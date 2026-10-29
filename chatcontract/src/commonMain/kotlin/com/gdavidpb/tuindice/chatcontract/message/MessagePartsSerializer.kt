package com.gdavidpb.tuindice.chatcontract.message

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonArray

// The parts of a message decode one by one. A part of a known type that carries a value this build does not know
// (an enum value a newer service added) becomes an UnknownMessagePart, and the rest of the message is kept.
internal object MessagePartsSerializer : KSerializer<List<MessagePart>> {
	private val parts = ListSerializer(MessagePart.serializer())

	override val descriptor: SerialDescriptor = parts.descriptor

	override fun deserialize(decoder: Decoder): List<MessagePart> {
		val input = decoder as? JsonDecoder ?: return parts.deserialize(decoder)

		return input.decodeJsonElement().jsonArray.map { element ->
			try {
				input.json.decodeFromJsonElement(MessagePart.serializer(), element)
			} catch (expected: SerializationException) {
				UnknownMessagePart
			}
		}
	}

	override fun serialize(encoder: Encoder, value: List<MessagePart>) {
		encoder.encodeSerializableValue(parts, value)
	}
}
