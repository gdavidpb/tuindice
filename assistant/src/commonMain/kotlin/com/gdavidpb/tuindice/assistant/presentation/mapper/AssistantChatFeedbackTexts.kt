package com.gdavidpb.tuindice.assistant.presentation.mapper

// The texts of the snackbars the feedback and the appeal answer with. The commands that produce them read these, so
// that the rows only forward a text that is ready to show.
interface AssistantChatFeedbackTexts {
	suspend fun rated(): String

	suspend fun reported(): String

	suspend fun reportTooLong(maxLength: Int): String

	suspend fun appealSent(): String

	suspend fun appealFailed(maxLength: Int): String
}
