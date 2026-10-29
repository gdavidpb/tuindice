package com.gdavidpb.tuindice.assistant.presentation.resource

import org.jetbrains.compose.resources.getString
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_chat_part_not_shown

// The resource-backed parts texts. The app binds it through AssistantChatResourceTexts, which carries it.
class AssistantChatPartResourceTexts : AssistantChatPartTexts {
	override suspend fun partNotShown(): String = getString(Res.string.assistant_chat_part_not_shown)
}
