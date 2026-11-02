package com.gdavidpb.tuindice.assistant.domain.usecase.param

import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentKind
import io.github.vinceglb.filekit.PlatformFile

/** The file the student picked, and whether the picker gave a photo or a PDF. */
data class PrepareAssistantAttachmentParams(
	val file: PlatformFile,
	val kind: AssistantAttachmentKind
)
