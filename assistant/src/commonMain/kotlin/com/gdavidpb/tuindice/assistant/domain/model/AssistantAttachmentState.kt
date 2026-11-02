package com.gdavidpb.tuindice.assistant.domain.model

/** Where an attachment of the message being written stands: read on the device, sent, kept, or failed. */
sealed interface AssistantAttachmentState {
	/** Being read and encoded on the device. */
	data object Preparing : AssistantAttachmentState

	/** Being sent to the service. */
	data object Uploading : AssistantAttachmentState

	/** Kept by the service under [attachmentId]: the message can carry it. */
	data class Ready(val attachmentId: String) : AssistantAttachmentState

	/** Not kept, for the [reason] the student can read. */
	data class Failed(val reason: AssistantAttachmentFailure) : AssistantAttachmentState
}
