package com.gdavidpb.tuindice.assistant.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed interface AssistantIntentError : UseCaseError {
	/** The intent is not among the open ones of the conversation. */
	data object IntentNotOpen : AssistantIntentError

	/** The intent is not of the kind the use case confirms. */
	data object IntentKindMismatch : AssistantIntentError
}
