package com.gdavidpb.tuindice.assistant.presentation.mapper

import org.jetbrains.compose.resources.getString
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_chat_appeal_failed
import tuindice.assistant.generated.resources.assistant_chat_appeal_sent
import tuindice.assistant.generated.resources.assistant_chat_rated
import tuindice.assistant.generated.resources.assistant_chat_report_too_long
import tuindice.assistant.generated.resources.assistant_chat_reported

// The feedback snackbars read from the module's compose resources: the app's binding of AssistantChatFeedbackTexts.
class AssistantChatFeedbackResourceTexts : AssistantChatFeedbackTexts {
	override suspend fun rated(): String = getString(Res.string.assistant_chat_rated)

	override suspend fun reported(): String = getString(Res.string.assistant_chat_reported)

	override suspend fun reportTooLong(maxLength: Int): String {
		return getString(Res.string.assistant_chat_report_too_long, maxLength)
	}

	override suspend fun appealSent(): String = getString(Res.string.assistant_chat_appeal_sent)

	override suspend fun appealFailed(maxLength: Int): String {
		return getString(Res.string.assistant_chat_appeal_failed, maxLength)
	}
}
