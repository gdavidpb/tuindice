package com.gdavidpb.tuindice.evaluations.presentation.model

sealed interface EvaluationsWeekKey {
	val sortOrder: Int
	val tagSuffix: String
	val weekNumber: Int?

	data object Continuous : EvaluationsWeekKey {
		override val sortOrder = 0
		override val tagSuffix = "continuous"
		override val weekNumber: Int? = null
	}

	data class Academic(
		override val weekNumber: Int
	) : EvaluationsWeekKey {
		override val sortOrder = weekNumber
		override val tagSuffix = "week_$weekNumber"
	}
}
