package com.gdavidpb.tuindice.assistant.domain.mapper

import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachment
import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentKind
import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentState
import com.gdavidpb.tuindice.assistant.domain.model.AssistantUserAttachment

// The attachment as the message keeps it: a photo keeps the image it was sent as, a PDF only its name and its pages.
internal fun AssistantAttachment.toUserAttachment(): AssistantUserAttachment {
	return AssistantUserAttachment(
		kind = kind,
		name = name,
		pageCount = pageCount,
		thumbnail = thumbnail,
		image = content?.bytes?.takeIf { kind == AssistantAttachmentKind.PHOTO }
	)
}

// The ids the service kept for the attachments, in the order they were attached. An attachment not kept has none.
internal fun List<AssistantAttachment>.attachmentIds(): List<String> {
	return mapNotNull { attachment -> (attachment.state as? AssistantAttachmentState.Ready)?.attachmentId }
}
