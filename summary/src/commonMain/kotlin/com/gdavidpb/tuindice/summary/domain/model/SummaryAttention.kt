package com.gdavidpb.tuindice.summary.domain.model

import kotlinx.datetime.LocalDate

/** Something that asks the student to look at the screen that owns it. */
sealed interface SummaryAttention {
	/**
	 * [count] evaluations whose day went by with no grade. [oldestWeekStart] is the Monday of the week
	 * of the oldest of them, and [oldestEvaluationId] is that evaluation: where the Evaluations tab
	 * takes the student, to the week and to the evaluation.
	 */
	data class UngradedEvaluations(
		val count: Int,
		val oldestWeekStart: LocalDate,
		val oldestEvaluationId: String
	) : SummaryAttention

	/** The university refused part of the enrollment of [subjectCode]; [error] is what it printed. */
	data class EnrollmentError(
		val subjectCode: String,
		val error: String
	) : SummaryAttention
}
