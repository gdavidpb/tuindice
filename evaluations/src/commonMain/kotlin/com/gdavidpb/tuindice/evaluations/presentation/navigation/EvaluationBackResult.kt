package com.gdavidpb.tuindice.evaluations.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class EvaluationBackResult {
	@Serializable
	data class SetGrade(
		val grade: Double
	) : EvaluationBackResult()

	@Serializable
	data class SetMaxGrade(
		val grade: Double
	) : EvaluationBackResult()
}
