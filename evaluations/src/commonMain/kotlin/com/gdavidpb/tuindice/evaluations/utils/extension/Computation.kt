package com.gdavidpb.tuindice.evaluations.utils.extension

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt
import kotlin.time.Instant

fun Double.toSubjectGrade() = when (roundToInt()) {
	in 30 until 50 -> 2
	in 50 until 70 -> 3
	in 70 until 85 -> 4
	in 85..Int.MAX_VALUE -> 5
	else -> 1
}

fun computeEvaluationState(
	scheduleMode: EvaluationScheduleMode,
	grade: Double?,
	date: Long?
): EvaluationState {
	val hasDatePassed = date != null && date.toLocalDate() < currentTimeMillis().toLocalDate()

	return when {
		scheduleMode == EvaluationScheduleMode.CONTINUOUS -> EvaluationState.CONTINUOUS
		grade != null -> EvaluationState.COMPLETED
		hasDatePassed -> EvaluationState.OVERDUE
		else -> EvaluationState.PENDING
	}
}

private fun Long.toLocalDate() = Instant
	.fromEpochMilliseconds(this)
	.toLocalDateTime(TimeZone.currentSystemDefault())
	.date
