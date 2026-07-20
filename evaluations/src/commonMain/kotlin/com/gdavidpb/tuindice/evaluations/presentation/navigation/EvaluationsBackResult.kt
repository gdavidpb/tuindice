package com.gdavidpb.tuindice.evaluations.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.NavResult
import kotlinx.serialization.Serializable

@Serializable
sealed class EvaluationsBackResult : NavResult {
	@Serializable
	data class SetEvaluationGrade(
		val evaluationId: String,
		val grade: Double
	) : EvaluationsBackResult()

	@Serializable
	data class RemoveEvaluation(
		val evaluationId: String
	) : EvaluationsBackResult()
}
