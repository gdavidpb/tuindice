package com.gdavidpb.tuindice.summary.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed interface UpdateTermReportsUseCaseError : UseCaseError {
	data object NoConnection : UpdateTermReportsUseCaseError
}
