package com.gdavidpb.tuindice.assistant.domain.model

/**
 * Why a photo was not kept: the service refuses it or cannot read it, the day's limit, a refusal the whole chat
 * answers, or anything else. Only [Other] can clear by trying again; the others give the same answer, so the photo is
 * taken out.
 */
sealed interface AssistantAttachmentFailure {
	/** Larger than the service takes once encoded. */
	data object TooLarge : AssistantAttachmentFailure

	/** Not a photo the service reads. */
	data object Unsupported : AssistantAttachmentFailure

	/** The day's uploads are used up; [retryAfterSeconds] is the wait the service gave, when it gave one. */
	data class DailyLimit(val retryAfterSeconds: Int?) : AssistantAttachmentFailure

	/** The service refused it for the chat's standing (its quota, a restriction or the guided mode), as the chat tells. */
	data object Refused : AssistantAttachmentFailure

	/** Anything else: no connection, or an answer this build does not name. */
	data object Other : AssistantAttachmentFailure
}
