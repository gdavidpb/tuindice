package com.gdavidpb.tuindice.evaluations.utils.extension

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationDateFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationStateFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationSubjectFilter
import com.gdavidpb.tuindice.evaluations.presentation.mapper.formatAsToNow
import com.gdavidpb.tuindice.evaluations.presentation.mapper.isDatePassed
import kotlin.math.roundToInt

fun Double.toSubjectGrade() = when (roundToInt()) {
	in 30 until 50 -> 2
	in 50 until 70 -> 3
	in 70 until 85 -> 4
	in 85..Integer.MAX_VALUE -> 5
	else -> 1
}

fun computeEvaluationState(grade: Double?, date: Long): EvaluationState {
	val hasDatePassed = date.isDatePassed()

	return when {
		date == 0L -> EvaluationState.CONTINUOUS
		grade == null && hasDatePassed -> EvaluationState.OVERDUE
		grade != null && hasDatePassed -> EvaluationState.COMPLETED
		else -> EvaluationState.PENDING
	}
}

fun List<Evaluation>.computeAvailableFilters(resourceResolver: ResourceResolver): List<EvaluationFilter> {
	val statesFilters = listOf(
		EvaluationStateFilter(
			label = resourceResolver.getString(R.string.label_state_pending)
		) { evaluation -> evaluation.state == EvaluationState.PENDING },
		EvaluationStateFilter(
			label = resourceResolver.getString(R.string.label_state_completed)
		) { evaluation -> evaluation.state == EvaluationState.COMPLETED },
		EvaluationStateFilter(
			label = resourceResolver.getString(R.string.label_state_not_grade)
		) { evaluation -> evaluation.state == EvaluationState.OVERDUE }
	)

	val subjectsFilters =
		map { evaluation -> evaluation.subject.code }
			.distinct()
			.map { subject -> EvaluationSubjectFilter(subject) }

	val datesFilters =
		map { evaluation -> evaluation.date.formatAsToNow() }
			.distinct()
			.map { date -> EvaluationDateFilter(date) }

	val filters = mutableListOf<EvaluationFilter>()

	filters.addAll(statesFilters)
	filters.addAll(subjectsFilters)
	filters.addAll(datesFilters)

	return filters
}