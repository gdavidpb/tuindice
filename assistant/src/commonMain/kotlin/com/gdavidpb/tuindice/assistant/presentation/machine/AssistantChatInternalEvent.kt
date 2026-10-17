package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversation
import com.gdavidpb.tuindice.assistant.domain.usecase.error.SendAssistantMessageError
import com.gdavidpb.tuindice.chatcontract.abuse.AbuseStanding
import com.gdavidpb.tuindice.chatcontract.event.RunErrorCode

/**
 * Internal machine inputs of the chat sheet: the decisions of Open, the availability and the conversation the store
 * observes, and the outcomes of the async commands. Each outcome is its own symbol, so every row keeps a fixed target.
 */
sealed interface AssistantChatInternalEvent {
	// The decision Open reached, taken from Idle.
	data object OpeningUnavailable : AssistantChatInternalEvent

	data object OpeningNeedsConsent : AssistantChatInternalEvent

	data object OpeningReady : AssistantChatInternalEvent

	// The availability observed while the sheet is open: off closes it; on tells whether it is guided.
	data object AssistantOff : AssistantChatInternalEvent

	data class AssistantOn(val guided: Boolean) : AssistantChatInternalEvent

	data object ConsentAccepted : AssistantChatInternalEvent

	data class ConversationObserved(val conversation: AssistantConversation) : AssistantChatInternalEvent

	// Outcomes of a send: an accepted turn is followed; a refusal either closes the sheet or lands in the content.
	data class TurnAccepted(val turnId: String, val lastSeq: Long) : AssistantChatInternalEvent

	data object SendDisabled : AssistantChatInternalEvent

	data object SendConsentRequired : AssistantChatInternalEvent

	data class SendRejected(
		val error: SendAssistantMessageError?,
		val sentText: String,
		val restoreText: String?,
		val suggestionId: String?
	) : AssistantChatInternalEvent

	// The turn followed to its end with a RunError, or its stream broke.
	data class TurnRunFailed(val code: RunErrorCode, val retryable: Boolean) : AssistantChatInternalEvent

	data class RestrictionChecked(val standing: AbuseStanding?) : AssistantChatInternalEvent

	data class IntentConfirmFinished(val intentId: String) : AssistantChatInternalEvent

	data class EnrollmentProofDelegated(val intentId: String) : AssistantChatInternalEvent

	// The answers to the student's feedback and appeal, each with the text of its snackbar.
	data class MessageRated(val message: String) : AssistantChatInternalEvent

	data class MessageReported(val message: String) : AssistantChatInternalEvent

	data class MessageReportRejected(val message: String) : AssistantChatInternalEvent

	data class AppealSent(val message: String) : AssistantChatInternalEvent

	data class AppealRejected(val message: String) : AssistantChatInternalEvent
}
