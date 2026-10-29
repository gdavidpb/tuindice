package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.datetime.LocalDate

/** Something that asks the student to look at the screen that owns it. */
sealed interface AcademicAttention {
	/**
	 * [count] evaluations whose day went by with no grade. [oldestWeekStart] is the Monday of the week of the
	 * oldest of them, and [oldestEvaluationId] is that evaluation: where the Evaluations tab takes the student,
	 * to the week and to the evaluation.
	 */
	data class UngradedEvaluations(
		val count: Int,
		val oldestWeekStart: LocalDate,
		val oldestEvaluationId: String
	) : AcademicAttention

	/**
	 * The university refused part of the enrollment of [subjectCode], on the attempt [attemptId]; [error] is
	 * what it printed.
	 */
	data class EnrollmentError(
		val subjectCode: String,
		val error: String,
		val attemptId: String
	) : AcademicAttention
}
