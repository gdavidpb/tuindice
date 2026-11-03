package com.gdavidpb.tuindice.assistant.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed interface PrepareAssistantAttachmentError : UseCaseError {
	/** Larger than the service takes once encoded. */
	data object TooLarge : PrepareAssistantAttachmentError
}
