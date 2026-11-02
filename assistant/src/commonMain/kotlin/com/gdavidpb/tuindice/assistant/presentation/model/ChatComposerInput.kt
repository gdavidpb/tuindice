package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachment

// What the composer shows of the message being written: its text, the revision that tells a replacement of the text
// from an echo of it, and its attachments. The machine reads them from its draft and hands them to the presentation.
data class ChatComposerInput(
	val text: String,
	val revision: Int,
	val attachments: List<AssistantAttachment>
)
