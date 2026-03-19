package com.gdavidpb.tuindice.summary.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed interface UpdateUserUseCaseError : UseCaseError {
	data object NotFound : UpdateUserUseCaseError
	data object Timeout : UpdateUserUseCaseError
	data object Unavailable : UpdateUserUseCaseError
	class NoConnection(val isNetworkAvailable: Boolean) : UpdateUserUseCaseError
}
