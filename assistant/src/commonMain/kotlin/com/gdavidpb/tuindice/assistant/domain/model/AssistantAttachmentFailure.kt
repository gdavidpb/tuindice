package com.gdavidpb.tuindice.assistant.domain.model

/** Why a photo was not kept: the service refuses it or cannot read it, the day's limit, or anything else. */
sealed interface AssistantAttachmentFailure {
	/** Larger than the service takes once encoded. */
	data object TooLarge : AssistantAttachmentFailure

	/** Not a photo the service reads. */
	data object Unsupported : AssistantAttachmentFailure

	/** The day's uploads are used up; [retryAfterSeconds] is the wait the service gave, when it gave one. */
	data class DailyLimit(val retryAfterSeconds: Int?) : AssistantAttachmentFailure

	/** Anything else: no connection, or an answer this build does not name. */
	data object Other : AssistantAttachmentFailure
}
