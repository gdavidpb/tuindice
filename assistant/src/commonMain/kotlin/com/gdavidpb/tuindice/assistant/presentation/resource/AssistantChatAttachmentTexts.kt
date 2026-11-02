package com.gdavidpb.tuindice.assistant.presentation.resource

import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentFailure

// The texts of the attachments of the message being written: the tray's reasons and details, and the limit's notice.
interface AssistantChatAttachmentTexts {
	// Why an attachment was not kept, as the tray says it.
	suspend fun attachmentFailure(failure: AssistantAttachmentFailure): String

	// The pages of a PDF, once the service has read them.
	suspend fun attachmentPages(pages: Int): String

	// What the chat says when one more attachment is picked than the message has room for.
	suspend fun attachmentLimit(): String
}
