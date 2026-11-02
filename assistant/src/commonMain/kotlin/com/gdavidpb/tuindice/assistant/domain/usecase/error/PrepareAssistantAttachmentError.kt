package com.gdavidpb.tuindice.assistant.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed interface PrepareAssistantAttachmentError : UseCaseError {
	/** Larger than the service takes: a PDF over its size, or a photo still over it once encoded. */
	data object TooLarge : PrepareAssistantAttachmentError

	/** Not a PDF, though it was picked as one. */
	data object Unsupported : PrepareAssistantAttachmentError
}
