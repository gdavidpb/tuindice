package com.gdavidpb.tuindice.evaluations.domain.usecase.error

sealed class EvaluationError {
	object EmptyName : EvaluationError()
	object SubjectMissed : EvaluationError()
	object GradeMissed : EvaluationError()
	object MaxGradeMissed : EvaluationError()
	object InvalidGradeStep : EvaluationError()
	object OutOfRangeGrade : EvaluationError()
	object TypeMissed : EvaluationError()
}