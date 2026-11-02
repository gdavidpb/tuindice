package com.gdavidpb.tuindice.assistant.domain.exception

import com.gdavidpb.tuindice.assistant.domain.usecase.error.PrepareAssistantAttachmentError

/** A file the student picked that cannot be sent as an attachment, found on the device before anything is uploaded. */
class AssistantAttachmentRejectedException(
	val error: PrepareAssistantAttachmentError
) : Exception()
