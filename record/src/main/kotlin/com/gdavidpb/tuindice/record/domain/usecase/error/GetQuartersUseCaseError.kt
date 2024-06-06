package com.gdavidpb.tuindice.record.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed class GetQuartersUseCaseError : UseCaseError {
	data object Timeout : GetQuartersUseCaseError()
	data object Unavailable : GetQuartersUseCaseError()
	data object OutdatedPassword : GetQuartersUseCaseError()
	class NoConnection(val isNetworkAvailable: Boolean) : GetQuartersUseCaseError()
}
