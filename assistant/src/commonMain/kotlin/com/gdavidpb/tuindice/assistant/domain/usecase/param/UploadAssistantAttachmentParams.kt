package com.gdavidpb.tuindice.assistant.domain.usecase.param

import com.gdavidpb.tuindice.assistant.domain.model.PreparedAssistantAttachment

/** The attachment read on the device, ready to be sent. */
data class UploadAssistantAttachmentParams(
	val attachment: PreparedAssistantAttachment
)
