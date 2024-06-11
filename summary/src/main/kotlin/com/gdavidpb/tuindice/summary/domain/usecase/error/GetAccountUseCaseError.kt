package com.gdavidpb.tuindice.summary.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed interface GetAccountUseCaseError : UseCaseError {
	data object Timeout : GetAccountUseCaseError
	data object Unavailable : GetAccountUseCaseError
	data object OutdatedPassword : GetAccountUseCaseError
	class NoConnection(val isNetworkAvailable: Boolean) : GetAccountUseCaseError
}