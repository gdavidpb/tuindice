package com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.assistant.domain.exception.AssistantRequestRefusedException
import com.gdavidpb.tuindice.assistant.domain.exception.SendAssistantMessageIllegalArgumentException
import com.gdavidpb.tuindice.assistant.domain.usecase.error.SendAssistantMessageError
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.chatcontract.error.AssistantErrorCodes

class SendAssistantMessageExceptionHandler : ExceptionHandler<SendAssistantMessageError> {
	override fun parseException(throwable: Throwable): SendAssistantMessageError? {
		return when (throwable) {
			is SendAssistantMessageIllegalArgumentException -> throwable.error
			is AssistantRequestRefusedException -> throwable.toSendError()
			else -> null
		}
	}

	// A refusal this build does not name is still an answer of the service, so it reads as Other rather than as a crash.
	private fun AssistantRequestRefusedException.toSendError(): SendAssistantMessageError {
		return when (code) {
			AssistantErrorCodes.ASSISTANT_DISABLED -> SendAssistantMessageError.AssistantDisabled(retryAfterSeconds)
			AssistantErrorCodes.CONSENT_REQUIRED -> SendAssistantMessageError.ConsentRequired
			AssistantErrorCodes.QUOTA_EXHAUSTED -> SendAssistantMessageError.QuotaExhausted(retryAfterSeconds)
			AssistantErrorCodes.SUSPENDED -> SendAssistantMessageError.Restricted
			AssistantErrorCodes.GUIDED_ONLY -> SendAssistantMessageError.GuidedOnly
			AssistantErrorCodes.TURN_IN_PROGRESS -> SendAssistantMessageError.TurnInProgress
			else -> SendAssistantMessageError.Other
		}
	}
}
