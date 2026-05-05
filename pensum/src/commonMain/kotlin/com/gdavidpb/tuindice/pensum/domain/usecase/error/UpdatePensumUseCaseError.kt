package com.gdavidpb.tuindice.pensum.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed interface UpdatePensumUseCaseError : UseCaseError {
	data object Timeout : UpdatePensumUseCaseError
	data object Unavailable : UpdatePensumUseCaseError
	class NoConnection(val isNetworkAvailable: Boolean) : UpdatePensumUseCaseError
}
