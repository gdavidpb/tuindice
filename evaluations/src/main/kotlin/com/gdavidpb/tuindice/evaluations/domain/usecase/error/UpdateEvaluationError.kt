package com.gdavidpb.tuindice.evaluations.domain.usecase.error

sealed class UpdateEvaluationError {
	object SubjectMissed : UpdateEvaluationError()
	object TypeMissed : UpdateEvaluationError()
	object MaxGradeMissed : UpdateEvaluationError()
}