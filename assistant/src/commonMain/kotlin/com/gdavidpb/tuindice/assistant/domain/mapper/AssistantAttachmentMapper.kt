package com.gdavidpb.tuindice.assistant.domain.mapper

import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachment
import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentState
import com.gdavidpb.tuindice.assistant.domain.model.AssistantUserAttachment

// The photo as the message keeps it: its name, its thumbnail, and the image it was sent as.
internal fun AssistantAttachment.toUserAttachment(): AssistantUserAttachment {
	return AssistantUserAttachment(
		name = name,
		thumbnail = thumbnail,
		image = content?.bytes
	)
}

// The ids the service kept for the attachments, in the order they were attached. An attachment not kept has none.
internal fun List<AssistantAttachment>.attachmentIds(): List<String> {
	return mapNotNull { attachment -> (attachment.state as? AssistantAttachmentState.Ready)?.attachmentId }
}
