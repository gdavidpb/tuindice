package com.gdavidpb.tuindice.evaluations.domain.usecase.error

import com.gdavidpb.tuindice.base.domain.usecase.base.Error

sealed class UpdateEvaluationError : Error {
	data object SubjectMissed : UpdateEvaluationError()
	data object TypeMissed : UpdateEvaluationError()
	data object MaxGradeMissed : UpdateEvaluationError()
}