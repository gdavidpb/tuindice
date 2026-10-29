package com.gdavidpb.tuindice.chatcontract

import com.gdavidpb.tuindice.chatcontract.component.ChatComponent
import com.gdavidpb.tuindice.chatcontract.component.UnknownChatComponent
import com.gdavidpb.tuindice.chatcontract.event.AssistantEvent
import com.gdavidpb.tuindice.chatcontract.event.UnknownAssistantEvent
import com.gdavidpb.tuindice.chatcontract.intent.IntentArgs
import com.gdavidpb.tuindice.chatcontract.intent.UnknownIntentArgs
import com.gdavidpb.tuindice.chatcontract.message.MessagePart
import com.gdavidpb.tuindice.chatcontract.message.UnknownMessagePart
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

/**
 * JSON format of the chat contract. A polymorphic type this build does not know (a component, intent, event or
 * message part added by a newer service) decodes to its `Unknown*` placeholder instead of failing the payload.
 * An enum value this build does not know falls back to the default of its property, when the property has one.
 */
public object ChatContract {
	public val json: Json = Json {
		classDiscriminator = "type"
		ignoreUnknownKeys = true
		explicitNulls = false
		encodeDefaults = true
		coerceInputValues = true
		serializersModule = SerializersModule {
			polymorphicDefaultDeserializer(ChatComponent::class) { UnknownChatComponent.serializer() }
			polymorphicDefaultDeserializer(IntentArgs::class) { UnknownIntentArgs.serializer() }
			polymorphicDefaultDeserializer(AssistantEvent::class) { UnknownAssistantEvent.serializer() }
			polymorphicDefaultDeserializer(MessagePart::class) { UnknownMessagePart.serializer() }
		}
	}
}
