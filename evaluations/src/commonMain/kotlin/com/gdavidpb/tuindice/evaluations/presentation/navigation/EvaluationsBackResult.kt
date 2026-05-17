package com.gdavidpb.tuindice.evaluations.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class EvaluationsBackResult {
	@Serializable
	data class SetEvaluationGrade(
		val evaluationId: String,
		val grade: Double
	) : EvaluationsBackResult()
}
