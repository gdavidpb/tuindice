package com.gdavidpb.tuindice.evaluations.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed interface EvaluationsUseCaseError : UseCaseError {
	data object Timeout : EvaluationsUseCaseError
	data object Unavailable : EvaluationsUseCaseError
	class NoConnection(val isNetworkAvailable: Boolean) : EvaluationsUseCaseError
}
