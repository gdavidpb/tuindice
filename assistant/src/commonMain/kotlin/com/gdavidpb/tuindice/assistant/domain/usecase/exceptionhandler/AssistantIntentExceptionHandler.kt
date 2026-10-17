package com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.assistant.domain.exception.AssistantIntentIllegalArgumentException
import com.gdavidpb.tuindice.assistant.domain.usecase.error.AssistantIntentError
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler

class AssistantIntentExceptionHandler : ExceptionHandler<AssistantIntentError> {
	override fun parseException(throwable: Throwable): AssistantIntentError? {
		return (throwable as? AssistantIntentIllegalArgumentException)?.error
	}
}
