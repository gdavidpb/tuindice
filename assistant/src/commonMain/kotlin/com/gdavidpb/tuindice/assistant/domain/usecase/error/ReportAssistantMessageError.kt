package com.gdavidpb.tuindice.assistant.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed interface ReportAssistantMessageError : UseCaseError {
	data object CommentTooLong : ReportAssistantMessageError
}
