@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.evaluations.utils.extension

import com.gdavidpb.tuindice.academiccore.domain.engine.EvaluationStateEngine
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationState
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

fun Double.toSubjectGrade() = when (roundToInt()) {
	in 30 until 50 -> 2
	in 50 until 70 -> 3
	in 70 until 85 -> 4
	in 85..Int.MAX_VALUE -> 5
	else -> 1
}

/** Adapts an evaluation's epoch date and the clock to the device's days and asks the engine. */
fun computeEvaluationState(
	scheduleMode: EvaluationScheduleMode,
	grade: Double?,
	date: Long?,
	clock: Clock
): EvaluationState {
	return EvaluationStateEngine.stateOf(
		scheduleMode = scheduleMode,
		hasGrade = grade != null,
		date = date?.toLocalDate(),
		today = clock.now().toEpochMilliseconds().toLocalDate()
	)
}

private fun Long.toLocalDate() = Instant
	.fromEpochMilliseconds(this)
	.toLocalDateTime(TimeZone.currentSystemDefault())
	.date
