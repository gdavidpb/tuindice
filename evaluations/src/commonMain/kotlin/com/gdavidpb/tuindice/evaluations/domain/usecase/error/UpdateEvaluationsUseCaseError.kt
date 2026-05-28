package com.gdavidpb.tuindice.evaluations.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed interface UpdateEvaluationsUseCaseError : UseCaseError {
	data object Timeout : UpdateEvaluationsUseCaseError
	data object Unavailable : UpdateEvaluationsUseCaseError
	class NoConnection(val isNetworkAvailable: Boolean) : UpdateEvaluationsUseCaseError
}
