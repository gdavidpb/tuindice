package com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.assistant.domain.usecase.error.PrepareAssistantAttachmentError
import com.gdavidpb.tuindice.base.domain.exception.EncodedImageTooLargeException
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler

class PrepareAssistantAttachmentExceptionHandler : ExceptionHandler<PrepareAssistantAttachmentError> {
	override fun parseException(throwable: Throwable): PrepareAssistantAttachmentError? {
		return when (throwable) {
			is EncodedImageTooLargeException -> PrepareAssistantAttachmentError.TooLarge
			// Both encoders refuse content they cannot decode as an invalid argument.
			is IllegalArgumentException -> PrepareAssistantAttachmentError.Unreadable
			else -> null
		}
	}
}
