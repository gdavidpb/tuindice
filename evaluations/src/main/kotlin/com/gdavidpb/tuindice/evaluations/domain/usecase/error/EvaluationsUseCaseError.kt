package com.gdavidpb.tuindice.evaluations.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed class EvaluationsUseCaseError : UseCaseError {
	data object NoSubjects : EvaluationsUseCaseError()
	data object Timeout : EvaluationsUseCaseError()
	data object Unavailable : EvaluationsUseCaseError()
	class NoConnection(val isNetworkAvailable: Boolean) : EvaluationsUseCaseError()
}