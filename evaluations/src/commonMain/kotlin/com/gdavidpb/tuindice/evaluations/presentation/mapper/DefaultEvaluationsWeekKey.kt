package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationTermDescriptor
import com.gdavidpb.tuindice.evaluations.presentation.utils.currentEvaluationLocalDate
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import kotlinx.datetime.LocalDate

fun defaultEvaluationsWeekKey(
	currentTerm: EvaluationTermDescriptor?,
	evaluations: List<Evaluation>,
	currentDate: LocalDate = currentEvaluationLocalDate()
): EvaluationsWeekKey {
	val hasDatedEvaluations = evaluations.any { evaluation ->
		evaluation.scheduleMode == EvaluationScheduleMode.DATED
	}

	return if (hasDatedEvaluations) {
		EvaluationsWeekKey.Academic(
			defaultEvaluationsWeekNumber(
				currentTerm = currentTerm,
				currentDate = currentDate
			)
		)
	} else {
		EvaluationsWeekKey.Continuous
	}
}
