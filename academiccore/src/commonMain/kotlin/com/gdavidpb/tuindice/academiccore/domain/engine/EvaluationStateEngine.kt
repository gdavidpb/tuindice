package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationState
import kotlinx.datetime.LocalDate

/** The state of an evaluation, told from its facts and the day it is asked on. */
object EvaluationStateEngine {
	/**
	 * Continuous evaluations are always [EvaluationState.CONTINUOUS]; otherwise a graded one is
	 * [EvaluationState.COMPLETED], an ungraded one whose [date] is before [today] is
	 * [EvaluationState.OVERDUE], and anything else is [EvaluationState.PENDING].
	 */
	fun stateOf(
		scheduleMode: EvaluationScheduleMode,
		hasGrade: Boolean,
		date: LocalDate?,
		today: LocalDate
	): EvaluationState {
		val hasDatePassed = date != null && date < today

		return when {
			scheduleMode == EvaluationScheduleMode.CONTINUOUS -> EvaluationState.CONTINUOUS
			hasGrade -> EvaluationState.COMPLETED
			hasDatePassed -> EvaluationState.OVERDUE
			else -> EvaluationState.PENDING
		}
	}
}
