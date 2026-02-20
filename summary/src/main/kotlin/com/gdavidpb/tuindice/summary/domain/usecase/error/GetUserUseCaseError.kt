package com.gdavidpb.tuindice.summary.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed interface GetUserUseCaseError : UseCaseError {
	data object Timeout : GetUserUseCaseError
	data object Unavailable : GetUserUseCaseError
	data object OutdatedPassword : GetUserUseCaseError
	class NoConnection(val isNetworkAvailable: Boolean) : GetUserUseCaseError
}