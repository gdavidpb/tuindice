package com.gdavidpb.tuindice.assistant.presentation.resource

// The texts of the snackbars the feedback and the appeal answer with. The commands that produce them read these, so
// that the rows only forward a text that is ready to show.
interface AssistantChatFeedbackTexts {
	suspend fun rated(): String

	suspend fun rateFailed(): String

	suspend fun stillAppliesFailed(): String

	suspend fun reported(): String

	suspend fun reportFailed(): String

	suspend fun reportTooLong(maxLength: Int): String

	suspend fun appealSent(): String

	// The length the appeal must keep: the text of a refusal for a length the appeal does not have.
	suspend fun appealFailed(maxLength: Int): String

	// The appeal did not reach the service: a network or a server failure, which says nothing about its length.
	suspend fun appealNotSent(): String
}
