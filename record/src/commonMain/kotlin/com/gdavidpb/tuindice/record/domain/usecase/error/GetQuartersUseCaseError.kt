package com.gdavidpb.tuindice.record.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed interface GetQuartersUseCaseError : UseCaseError {
	data object Timeout : GetQuartersUseCaseError
	data object Unavailable : GetQuartersUseCaseError
	class NoConnection(val isNetworkAvailable: Boolean) : GetQuartersUseCaseError
}
