package com.gdavidpb.tuindice.assistant.domain.usecase.param

import io.github.vinceglb.filekit.PlatformFile

/** The photo the student picked. */
data class PrepareAssistantAttachmentParams(
	val file: PlatformFile
)
