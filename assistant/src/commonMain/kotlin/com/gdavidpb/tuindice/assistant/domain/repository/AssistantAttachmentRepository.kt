package com.gdavidpb.tuindice.assistant.domain.repository

import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentContent
import com.gdavidpb.tuindice.chatcontract.attachment.AttachmentAccepted

/** Sends the bytes of an attachment to the service, which keeps them for the student and answers with its id. */
interface AssistantAttachmentRepository {
	suspend fun upload(content: AssistantAttachmentContent, name: String): AttachmentAccepted
}
