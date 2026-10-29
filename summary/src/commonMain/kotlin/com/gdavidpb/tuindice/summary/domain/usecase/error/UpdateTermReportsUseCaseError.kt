package com.gdavidpb.tuindice.summary.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed interface UpdateTermReportsUseCaseError : UseCaseError {
	data object NoConnection : UpdateTermReportsUseCaseError

	// The service refused the session: it ended, or its account is no longer active.
	data object SessionEnded : UpdateTermReportsUseCaseError
}
