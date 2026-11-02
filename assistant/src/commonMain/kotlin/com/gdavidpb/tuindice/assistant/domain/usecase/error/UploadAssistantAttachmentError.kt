package com.gdavidpb.tuindice.assistant.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed interface UploadAssistantAttachmentError : UseCaseError {
	/** [retryAfterSeconds] is the wait the server gave, when it gave one. */
	data class AssistantDisabled(val retryAfterSeconds: Int?) : UploadAssistantAttachmentError

	data object ConsentRequired : UploadAssistantAttachmentError

	/** [retryAfterSeconds] is the wait the server gave before the quota comes back, when it gave one. */
	data class QuotaExhausted(val retryAfterSeconds: Int?) : UploadAssistantAttachmentError

	data object Restricted : UploadAssistantAttachmentError

	data object GuidedOnly : UploadAssistantAttachmentError

	/** The file is larger than the service takes. */
	data object TooLarge : UploadAssistantAttachmentError

	/** The file is not a photo or a PDF the service reads. */
	data object Unsupported : UploadAssistantAttachmentError

	/** The PDF has too many pages, is protected with a password, or cannot be opened. */
	data object Unreadable : UploadAssistantAttachmentError

	/** The student uploaded as many attachments as a day allows; [retryAfterSeconds] is the wait the server gave. */
	data class DailyLimit(val retryAfterSeconds: Int?) : UploadAssistantAttachmentError

	data object Other : UploadAssistantAttachmentError
}
