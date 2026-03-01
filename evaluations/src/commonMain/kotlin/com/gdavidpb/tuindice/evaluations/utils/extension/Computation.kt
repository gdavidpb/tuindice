package com.gdavidpb.tuindice.evaluations.utils.extension

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationDateFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationStateFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationSubjectFilter
import com.gdavidpb.tuindice.evaluations.presentation.mapper.formatAsToNow
import kotlin.math.roundToInt

fun Double.toSubjectGrade() = when (roundToInt()) {
	in 30 until 50 -> 2
	in 50 until 70 -> 3
	in 70 until 85 -> 4
	in 85..Int.MAX_VALUE -> 5
	else -> 1
}

fun computeEvaluationState(grade: Double?, date: Long?): EvaluationState {
	val hasDatePassed = date != null && date < currentTimeMillis()

	return when {
		date == null -> EvaluationState.CONTINUOUS
		grade == null && hasDatePassed -> EvaluationState.OVERDUE
		grade != null && hasDatePassed -> EvaluationState.COMPLETED
		else -> EvaluationState.PENDING
	}
}

fun List<Evaluation>.computeAvailableFilters(
	pendingLabel: String,
	completedLabel: String,
	noGradeLabel: String
): List<EvaluationFilter> {
	val statesFilters = listOf(
		EvaluationStateFilter(
			label = pendingLabel
		) { evaluation -> evaluation.state == EvaluationState.PENDING },
		EvaluationStateFilter(
			label = completedLabel
		) { evaluation -> evaluation.state == EvaluationState.COMPLETED },
		EvaluationStateFilter(
			label = noGradeLabel
		) { evaluation -> evaluation.state == EvaluationState.OVERDUE }
	)

	val subjectsFilters =
		map { evaluation -> evaluation.subjectCode }
			.distinct()
			.map { subject -> EvaluationSubjectFilter(subject) }

	val datesFilters =
		map { evaluation -> evaluation.date.formatAsToNow() }
			.distinct()
			.map { label ->
				EvaluationDateFilter(label) { evaluation ->
					evaluation.date.formatAsToNow() == label
				}
			}

	val filters = mutableListOf<EvaluationFilter>()

	filters.addAll(statesFilters)
	filters.addAll(subjectsFilters)
	filters.addAll(datesFilters)

	return filters
}
