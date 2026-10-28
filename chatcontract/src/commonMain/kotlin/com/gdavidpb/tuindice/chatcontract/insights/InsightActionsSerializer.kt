package com.gdavidpb.tuindice.chatcontract.insights

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

/**
 * Reads the buttons of "Qué sigue" one at a time. An action this build cannot read, because it opens a screen or
 * names a record kind that a newer service added, is left out, and the rest of the report still arrives. Without
 * this, one such action fails the whole report. The wire format is the one of a plain list of [InsightAction].
 */
internal object InsightActionsSerializer : KSerializer<List<InsightAction>> {

	private val list = ListSerializer(InsightAction.serializer())

	override val descriptor: SerialDescriptor = list.descriptor

	override fun serialize(encoder: Encoder, value: List<InsightAction>) {
		list.serialize(encoder, value)
	}

	override fun deserialize(decoder: Decoder): List<InsightAction> {
		val input = decoder as? JsonDecoder ?: return list.deserialize(decoder)

		return actionsOf(input.decodeJsonElement()).mapNotNull { element ->
			try {
				input.json.decodeFromJsonElement(InsightAction.serializer(), element)
			} catch (ignored: SerializationException) {
				null
			}
		}
	}

	// An insight whose actions are null has no buttons, so it arrives with none: the same as one without the field. The
	// report itself is not lost to a value this build does not expect.
	private fun actionsOf(element: JsonElement): List<JsonElement> = when (element) {
		is JsonNull -> emptyList()
		is JsonArray -> element
		else -> throw SerializationException("The actions of an insight must be a JSON array")
	}
}
