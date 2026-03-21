package com.gdavidpb.tuindice.record.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed interface UpdateQuartersUseCaseError : UseCaseError {
	data object Timeout : UpdateQuartersUseCaseError
	data object Unavailable : UpdateQuartersUseCaseError
	class NoConnection(val isNetworkAvailable: Boolean) : UpdateQuartersUseCaseError
}
