package com.gdavidpb.tuindice.chatcontract.message

import com.gdavidpb.tuindice.chatcontract.component.ChatComponent
import com.gdavidpb.tuindice.chatcontract.component.UnknownChatComponent
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

// A component of a known type that carries a value this build does not know decodes to an UnknownChatComponent, which
// keeps its id and its fallback text: the message shows that text in the component's place.
internal object ComponentOrFallbackSerializer : KSerializer<ChatComponent> {
	override val descriptor: SerialDescriptor = ChatComponent.serializer().descriptor

	override fun deserialize(decoder: Decoder): ChatComponent {
		val input = decoder as? JsonDecoder ?: return ChatComponent.serializer().deserialize(decoder)
		val element = input.decodeJsonElement()

		return try {
			input.json.decodeFromJsonElement(ChatComponent.serializer(), element)
		} catch (expected: SerializationException) {
			element.unknownComponent()
		}
	}

	override fun serialize(encoder: Encoder, value: ChatComponent) {
		encoder.encodeSerializableValue(ChatComponent.serializer(), value)
	}
}

private fun JsonElement.unknownComponent(): ChatComponent {
	val fields = this as? JsonObject

	return UnknownChatComponent(id = fields.textOf("id"), fallbackText = fields.textOf("fallbackText"))
}

private fun JsonObject?.textOf(key: String): String = (this?.get(key) as? JsonPrimitive)?.contentOrNull.orEmpty()
