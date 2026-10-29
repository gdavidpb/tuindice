package com.gdavidpb.tuindice.assistant.presentation.resource

import org.jetbrains.compose.resources.getString
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_chat_appeal_failed
import tuindice.assistant.generated.resources.assistant_chat_appeal_sent
import tuindice.assistant.generated.resources.assistant_chat_rate_failed
import tuindice.assistant.generated.resources.assistant_chat_rated
import tuindice.assistant.generated.resources.assistant_chat_report_failed
import tuindice.assistant.generated.resources.assistant_chat_report_too_long
import tuindice.assistant.generated.resources.assistant_chat_reported
import tuindice.assistant.generated.resources.assistant_chat_send_failed
import tuindice.assistant.generated.resources.assistant_chat_still_applies_failed

// The feedback snackbars read from the module's compose resources: the app's binding of AssistantChatFeedbackTexts.
class AssistantChatFeedbackResourceTexts : AssistantChatFeedbackTexts {
	override suspend fun rated(): String = getString(Res.string.assistant_chat_rated)

	override suspend fun rateFailed(): String = getString(Res.string.assistant_chat_rate_failed)

	override suspend fun stillAppliesFailed(): String = getString(Res.string.assistant_chat_still_applies_failed)

	override suspend fun reported(): String = getString(Res.string.assistant_chat_reported)

	override suspend fun reportFailed(): String = getString(Res.string.assistant_chat_report_failed)

	override suspend fun reportTooLong(maxLength: Int): String {
		return getString(Res.string.assistant_chat_report_too_long, maxLength)
	}

	override suspend fun appealSent(): String = getString(Res.string.assistant_chat_appeal_sent)

	override suspend fun appealFailed(maxLength: Int): String {
		return getString(Res.string.assistant_chat_appeal_failed, maxLength)
	}

	// The chat's failed-send text: the appeal is sent as a message is, so a failure to reach the service reads the same.
	override suspend fun appealNotSent(): String = getString(Res.string.assistant_chat_send_failed)
}
