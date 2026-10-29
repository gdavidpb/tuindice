package com.gdavidpb.tuindice.assistant.presentation.resource

import org.jetbrains.compose.resources.getString
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_chat_confirm_failed
import tuindice.assistant.generated.resources.assistant_chat_dismiss_failed

// The resource-backed intent texts. The app binds it through AssistantChatResourceTexts, which carries it.
class AssistantChatIntentResourceTexts : AssistantChatIntentTexts {
	override suspend fun confirmFailed(): String = getString(Res.string.assistant_chat_confirm_failed)

	override suspend fun dismissFailed(): String = getString(Res.string.assistant_chat_dismiss_failed)
}
