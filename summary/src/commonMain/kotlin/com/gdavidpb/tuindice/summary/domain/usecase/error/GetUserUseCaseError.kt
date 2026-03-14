package com.gdavidpb.tuindice.summary.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed interface GetUserUseCaseError : UseCaseError {
	data object NotFound : GetUserUseCaseError
	data object Timeout : GetUserUseCaseError
	data object Unavailable : GetUserUseCaseError
	class NoConnection(val isNetworkAvailable: Boolean) : GetUserUseCaseError
}
