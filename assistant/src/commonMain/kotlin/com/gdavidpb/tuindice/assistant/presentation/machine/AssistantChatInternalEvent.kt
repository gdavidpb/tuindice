package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversation
import com.gdavidpb.tuindice.assistant.domain.usecase.error.ExtractEvaluationsFromPhotoError
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

	// The consent could not be given: the sheet says so and asks again.
	data object ConsentAcceptFailed : AssistantChatInternalEvent

	data class ConversationObserved(val conversation: AssistantConversation) : AssistantChatInternalEvent

	// Outcomes of a send: an accepted turn is followed; a refusal either closes the sheet or lands in the content.
	data class TurnAccepted(
		val turnId: String,
		val lastSeq: Long,
		val suggestionId: String? = null
	) : AssistantChatInternalEvent

	data object SendDisabled : AssistantChatInternalEvent

	data object SendConsentRequired : AssistantChatInternalEvent

	data class SendRejected(
		val error: SendAssistantMessageError?,
		val sentText: String,
		val restoreText: String?,
		val suggestionId: String?,
		val clientTurnId: String
	) : AssistantChatInternalEvent

	// The turn followed to its end with a RunError, or its stream broke.
	data class TurnRunFailed(val code: RunErrorCode, val retryable: Boolean) : AssistantChatInternalEvent

	data class RestrictionChecked(val standing: AbuseStanding?) : AssistantChatInternalEvent

	data class IntentConfirmFinished(val intentId: String) : AssistantChatInternalEvent

	data class EnrollmentProofDelegated(val intentId: String) : AssistantChatInternalEvent

	// The service saved the answer to a «¿Sigue aplicando?» question.
	data class StillAppliesSaved(val memoryId: String, val applies: Boolean) : AssistantChatInternalEvent

	// The service refused the answer about [memoryId]: the state of the memory is read again to tell if it still waits.
	data class StillAppliesFailed(val memoryId: String) : AssistantChatInternalEvent

	// The states of the memories the service lists, by memory id.
	data class MemoryStatesLoaded(val states: Map<String, String>) : AssistantChatInternalEvent

	// The answers to the student's feedback and appeal, each with the text of its snackbar.
	data class MessageRated(val message: String) : AssistantChatInternalEvent

	// The service refused the thumbs [rejected] on [messageId]: the thumbs go back to [previous], if the student has not
	// rated it again since, and the snackbar says so.
	data class MessageRateFailed(
		val messageId: String,
		val rejected: Boolean,
		val previous: Boolean?,
		val message: String
	) : AssistantChatInternalEvent

	// The service saved the report of [messageId]: the message shows as reported, and the snackbar says so.
	data class MessageReported(val messageId: String, val message: String) : AssistantChatInternalEvent

	// The service refused the report, or could not be reached: the message stays as it was, and the snackbar says so.
	data class MessageReportFailed(val message: String) : AssistantChatInternalEvent

	data class MessageReportRejected(val message: String) : AssistantChatInternalEvent

	data class AppealSent(val message: String) : AssistantChatInternalEvent

	data class AppealRejected(val message: String) : AssistantChatInternalEvent

	// A photo was read: its proposal is in the conversation, and the reading ends.
	data object PhotoExtracted : AssistantChatInternalEvent

	// A photo was not read: the error says which notice the student sees.
	data class PhotoRejected(val error: ExtractEvaluationsFromPhotoError?) : AssistantChatInternalEvent

	// The platform's answer to whether the notification permission is offered now, read when the content opens.
	data class NotificationOfferChecked(val needed: Boolean) : AssistantChatInternalEvent
}
