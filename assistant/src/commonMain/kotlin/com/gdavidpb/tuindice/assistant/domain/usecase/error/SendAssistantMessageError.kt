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

	data object InvalidMessage : SendAssistantMessageError

	data object Other : SendAssistantMessageError
}
