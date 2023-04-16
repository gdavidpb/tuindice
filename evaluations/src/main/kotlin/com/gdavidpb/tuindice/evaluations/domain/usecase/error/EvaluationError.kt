package com.gdavidpb.tuindice.evaluations.domain.usecase.error

sealed class EvaluationError {
	object EmptyName : EvaluationError()
	object GradeMissed : EvaluationError()
	object InvalidGradeStep : EvaluationError()
	object OutOfRangeGrade : EvaluationError()
	object DateMissed : EvaluationError()
	object TypeMissed : EvaluationError()
}