package com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.assistant.domain.exception.ReportAssistantMessageIllegalArgumentException
import com.gdavidpb.tuindice.assistant.domain.usecase.error.ReportAssistantMessageError
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler

class ReportAssistantMessageExceptionHandler : ExceptionHandler<ReportAssistantMessageError> {
	override fun parseException(throwable: Throwable): ReportAssistantMessageError? {
		return (throwable as? ReportAssistantMessageIllegalArgumentException)?.error
	}
}
