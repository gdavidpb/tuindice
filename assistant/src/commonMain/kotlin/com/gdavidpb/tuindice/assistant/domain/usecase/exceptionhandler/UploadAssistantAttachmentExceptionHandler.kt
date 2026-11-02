package com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.assistant.domain.exception.AssistantRequestRefusedException
import com.gdavidpb.tuindice.assistant.domain.usecase.error.UploadAssistantAttachmentError
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.chatcontract.error.AssistantErrorCodes

class UploadAssistantAttachmentExceptionHandler : ExceptionHandler<UploadAssistantAttachmentError> {
	override fun parseException(throwable: Throwable): UploadAssistantAttachmentError? {
		return when (throwable) {
			is AssistantRequestRefusedException -> throwable.toUploadError()
			else -> null
		}
	}

	// The codes are the ones of a turn plus those of an attachment. A refusal this build does not name is still an answer
	// of the service: Other.
	private fun AssistantRequestRefusedException.toUploadError(): UploadAssistantAttachmentError {
		return when (code) {
			AssistantErrorCodes.ASSISTANT_DISABLED -> UploadAssistantAttachmentError.AssistantDisabled(retryAfterSeconds)
			AssistantErrorCodes.CONSENT_REQUIRED -> UploadAssistantAttachmentError.ConsentRequired
			AssistantErrorCodes.QUOTA_EXHAUSTED -> UploadAssistantAttachmentError.QuotaExhausted(retryAfterSeconds)
			AssistantErrorCodes.SUSPENDED -> UploadAssistantAttachmentError.Restricted
			AssistantErrorCodes.GUIDED_ONLY -> UploadAssistantAttachmentError.GuidedOnly
			AssistantErrorCodes.ATTACHMENT_TOO_LARGE -> UploadAssistantAttachmentError.TooLarge
			AssistantErrorCodes.ATTACHMENT_UNSUPPORTED -> UploadAssistantAttachmentError.Unsupported
			AssistantErrorCodes.ATTACHMENT_UNREADABLE -> UploadAssistantAttachmentError.Unreadable
			AssistantErrorCodes.ATTACHMENT_DAILY_LIMIT -> UploadAssistantAttachmentError.DailyLimit(retryAfterSeconds)
			else -> UploadAssistantAttachmentError.Other
		}
	}
}
