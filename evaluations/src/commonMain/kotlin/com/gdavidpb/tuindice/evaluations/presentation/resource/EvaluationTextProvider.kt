package com.gdavidpb.tuindice.evaluations.presentation.resource

interface EvaluationTextProvider {
	fun defaultError(): String
	fun serviceUnavailable(): String
	fun networkUnavailable(): String
	fun timeout(): String
	fun evaluationAdded(): String
	fun evaluationUpdated(): String
	fun evaluationRemoved(): String
	fun evaluationGradeUpdated(): String
	fun evaluationSubjectMissed(): String
	fun evaluationTypeMissed(): String
	fun evaluationMaxGradeMissed(): String
}
