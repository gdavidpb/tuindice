package com.gdavidpb.tuindice.assistant.presentation.resource

import org.jetbrains.compose.resources.getString
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_chat_sending
import tuindice.assistant.generated.resources.assistant_photo_reading

// The resource-backed progress texts. The app binds it through AssistantChatResourceTexts, which carries it.
class AssistantChatProgressResourceTexts : AssistantChatProgressTexts {
	override suspend fun photoReading(): String = getString(Res.string.assistant_photo_reading)

	override suspend fun sending(): String = getString(Res.string.assistant_chat_sending)
}
