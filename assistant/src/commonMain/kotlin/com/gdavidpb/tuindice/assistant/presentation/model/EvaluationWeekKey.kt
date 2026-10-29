package com.gdavidpb.tuindice.assistant.presentation.model

// The week an evaluation of a sheet falls in: one of the weeks of the term, or the continuous evaluations, which hold
// no week. The tag suffix names the week on screen, as the Evaluations tab names its own weeks.
sealed interface EvaluationWeekKey {
	val tagSuffix: String

	data object Continuous : EvaluationWeekKey {
		override val tagSuffix = "continuous"
	}

	data class Academic(val weekNumber: Int) : EvaluationWeekKey {
		override val tagSuffix = "week_$weekNumber"
	}
}
