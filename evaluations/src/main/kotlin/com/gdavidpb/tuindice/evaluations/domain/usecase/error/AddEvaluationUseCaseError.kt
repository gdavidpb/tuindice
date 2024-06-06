package com.gdavidpb.tuindice.evaluations.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError

sealed class AddEvaluationUseCaseError : UseCaseError {
	data object SubjectMissed : AddEvaluationUseCaseError()
	data object TypeMissed : AddEvaluationUseCaseError()
	data object MaxGradeMissed : AddEvaluationUseCaseError()
}