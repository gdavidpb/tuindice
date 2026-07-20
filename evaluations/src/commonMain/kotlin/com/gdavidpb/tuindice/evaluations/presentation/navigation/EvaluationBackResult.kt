package com.gdavidpb.tuindice.evaluations.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.NavResult
import kotlinx.serialization.Serializable

@Serializable
sealed class EvaluationBackResult : NavResult {
	@Serializable
	data class SetGrade(
		val grade: Double
	) : EvaluationBackResult()

	@Serializable
	data class SetMaxGrade(
		val grade: Double
	) : EvaluationBackResult()
}
