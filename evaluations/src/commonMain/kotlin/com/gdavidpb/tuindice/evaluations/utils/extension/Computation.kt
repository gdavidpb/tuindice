@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.evaluations.utils.extension

import com.gdavidpb.tuindice.academiccore.domain.engine.EvaluationStateEngine
import com.gdavidpb.tuindice.academiccore.domain.engine.SubjectGradeScale
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.utils.toLocalDate
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

fun Double.toSubjectGrade() = SubjectGradeScale.gradeOf(this)

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
