package com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.assistant.domain.exception.AssistantAttachmentRejectedException
import com.gdavidpb.tuindice.assistant.domain.usecase.error.PrepareAssistantAttachmentError
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler

class PrepareAssistantAttachmentExceptionHandler : ExceptionHandler<PrepareAssistantAttachmentError> {
	override fun parseException(throwable: Throwable): PrepareAssistantAttachmentError? {
		return (throwable as? AssistantAttachmentRejectedException)?.error
	}
}
