package com.gdavidpb.tuindice.assistant.presentation.resource

import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentFailure

// The texts of the photos of the message being written: the tray's reasons, and the chat's notices about them.
interface AssistantChatAttachmentTexts {
	// Why a photo was not kept, as the tray says it.
	suspend fun attachmentFailure(failure: AssistantAttachmentFailure): String

	// What the chat says when one more photo is picked than the message has room for.
	suspend fun attachmentLimit(): String

	// What the chat says when the service refuses the photos of a message: they are back in the tray.
	suspend fun attachmentsRefused(): String

	// What the chat says when a photo of the message is no longer kept: the message goes back to the composer.
	suspend fun attachmentExpired(): String
}
