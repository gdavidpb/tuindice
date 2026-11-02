package com.gdavidpb.tuindice.assistant.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed interface SendAssistantMessageError : UseCaseError {
	/** [retryAfterSeconds] is the wait the server gave, when it gave one. */
	data class AssistantDisabled(val retryAfterSeconds: Int?) : SendAssistantMessageError

	data object ConsentRequired : SendAssistantMessageError

	/** [retryAfterSeconds] is the wait the server gave before the quota comes back, when it gave one. */
	data class QuotaExhausted(val retryAfterSeconds: Int?) : SendAssistantMessageError

	data object Restricted : SendAssistantMessageError

	data object GuidedOnly : SendAssistantMessageError

	data object TurnInProgress : SendAssistantMessageError

	/**
	 * The service refused the attachments of the message and the message is not sent. [expired] says one of them is no
	 * longer kept, so it has to be attached again; otherwise the message carries too many, or too heavy, attachments.
	 */
	data class AttachmentsRefused(val expired: Boolean) : SendAssistantMessageError

	data object InvalidMessage : SendAssistantMessageError

	data object Other : SendAssistantMessageError
}
