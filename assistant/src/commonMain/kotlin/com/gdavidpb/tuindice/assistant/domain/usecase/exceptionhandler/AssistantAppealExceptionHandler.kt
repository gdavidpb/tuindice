package com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.assistant.domain.exception.AssistantAppealIllegalArgumentException
import com.gdavidpb.tuindice.assistant.domain.usecase.error.AssistantAppealError
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler

class AssistantAppealExceptionHandler : ExceptionHandler<AssistantAppealError> {
	override fun parseException(throwable: Throwable): AssistantAppealError? {
		return (throwable as? AssistantAppealIllegalArgumentException)?.error
	}
}
