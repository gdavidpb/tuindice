package com.gdavidpb.tuindice.assistant.domain.model

/** Why an attachment was not kept: a file the service refuses or cannot read, the day's limit, or anything else. */
sealed interface AssistantAttachmentFailure {
	/** Larger than the service takes: a PDF over its size, or a photo still over its size once encoded. */
	data object TooLarge : AssistantAttachmentFailure

	/** Not a photo or a PDF the service reads. */
	data object Unsupported : AssistantAttachmentFailure

	/** A PDF the service cannot open: too many pages, or protected with a password. */
	data object Unreadable : AssistantAttachmentFailure

	/** The day's uploads are used up; [retryAfterSeconds] is the wait the service gave, when it gave one. */
	data class DailyLimit(val retryAfterSeconds: Int?) : AssistantAttachmentFailure

	/** Anything else: no connection, or an answer this build does not name. */
	data object Other : AssistantAttachmentFailure
}
