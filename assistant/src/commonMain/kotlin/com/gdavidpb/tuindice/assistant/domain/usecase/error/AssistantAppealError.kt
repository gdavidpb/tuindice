package com.gdavidpb.tuindice.assistant.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed interface AssistantAppealError : UseCaseError {
	data object Empty : AssistantAppealError

	data object TooLong : AssistantAppealError
}
