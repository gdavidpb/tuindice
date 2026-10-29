package com.gdavidpb.tuindice.assistant.domain.usecase.param

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import io.github.vinceglb.filekit.PlatformFile

/** The photo the student took or picked, and the screen the chat was opened from, if any. */
data class ExtractEvaluationsFromPhotoParams(
	val file: PlatformFile,
	val screenContext: AssistantScreenContext?
)
