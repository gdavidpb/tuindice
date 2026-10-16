package com.gdavidpb.tuindice.summary.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed interface ShareTermImageUseCaseError : UseCaseError {
	// The picture could not be encoded or could not be handed to the platform's share sheet.
	data object ImageUnavailable : ShareTermImageUseCaseError
}
