package com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.assistant.domain.exception.AssistantRequestRefusedException
import com.gdavidpb.tuindice.assistant.domain.usecase.error.ExtractEvaluationsFromPhotoError
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.chatcontract.error.AssistantErrorCodes

class ExtractEvaluationsFromPhotoExceptionHandler : ExceptionHandler<ExtractEvaluationsFromPhotoError> {
	override fun parseException(throwable: Throwable): ExtractEvaluationsFromPhotoError? {
		return when (throwable) {
			is AssistantRequestRefusedException -> throwable.toExtractionError()
			else -> null
		}
	}

	// The codes are the ones of a turn. A refusal this build does not name is still an answer of the service: Other.
	private fun AssistantRequestRefusedException.toExtractionError(): ExtractEvaluationsFromPhotoError {
		return when (code) {
			AssistantErrorCodes.ASSISTANT_DISABLED -> ExtractEvaluationsFromPhotoError.AssistantDisabled(retryAfterSeconds)
			AssistantErrorCodes.CONSENT_REQUIRED -> ExtractEvaluationsFromPhotoError.ConsentRequired
			AssistantErrorCodes.QUOTA_EXHAUSTED -> ExtractEvaluationsFromPhotoError.QuotaExhausted(retryAfterSeconds)
			AssistantErrorCodes.SUSPENDED -> ExtractEvaluationsFromPhotoError.Restricted
			AssistantErrorCodes.GUIDED_ONLY -> ExtractEvaluationsFromPhotoError.GuidedOnly
			else -> ExtractEvaluationsFromPhotoError.Other
		}
	}
}
