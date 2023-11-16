package com.gdavidpb.tuindice.evaluations.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.Error

sealed class AddEvaluationError : Error {
	data object SubjectMissed : AddEvaluationError()
	data object TypeMissed : AddEvaluationError()
	data object MaxGradeMissed : AddEvaluationError()
}