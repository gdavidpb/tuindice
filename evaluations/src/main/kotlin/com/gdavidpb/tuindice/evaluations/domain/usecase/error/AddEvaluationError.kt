package com.gdavidpb.tuindice.evaluations.domain.usecase.error

sealed class AddEvaluationError {
	object SubjectMissed : AddEvaluationError()
	object TypeMissed : AddEvaluationError()
	object MaxGradeMissed : AddEvaluationError()
}