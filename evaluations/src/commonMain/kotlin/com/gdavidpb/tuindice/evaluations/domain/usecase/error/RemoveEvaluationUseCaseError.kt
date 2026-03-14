package com.gdavidpb.tuindice.evaluations.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed interface RemoveEvaluationUseCaseError : UseCaseError {
	data object NotFound : RemoveEvaluationUseCaseError
}
