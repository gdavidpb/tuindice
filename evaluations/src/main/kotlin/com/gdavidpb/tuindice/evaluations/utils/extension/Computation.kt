package com.gdavidpb.tuindice.evaluations.utils.extension

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationDateFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationStateFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationSubjectFilter
import com.gdavidpb.tuindice.evaluations.presentation.mapper.dateGroup
import java.util.Date
import kotlin.math.roundToInt

fun Double.toSubjectGrade() = when (roundToInt()) {
	in 30 until 50 -> 2
	in 50 until 70 -> 3
	in 70 until 85 -> 4
	in 85..Integer.MAX_VALUE -> 5
	else -> 1
}

fun Date.isOverdue() = time != 0L && Date().after(this)

fun List<Evaluation>.computeAvailableFilters(resourceResolver: ResourceResolver): List<EvaluationFilter> {
	val statesFilters = listOf(
		EvaluationStateFilter(
			label = resourceResolver.getString(R.string.label_state_pending)
		) { evaluation -> !evaluation.isCompleted },
		EvaluationStateFilter(
			label = resourceResolver.getString(R.string.label_state_completed)
		) { evaluation -> evaluation.isCompleted },
		EvaluationStateFilter(
			label = resourceResolver.getString(R.string.label_state_not_grade)
		) { evaluation -> evaluation.isNotGraded }
	)

	val subjectsFilters =
		map { evaluation -> evaluation.subjectCode }
			.distinct()
			.map { subject -> EvaluationSubjectFilter(subject) }

	val datesFilters =
		map { evaluation -> evaluation.date.dateGroup() }
			.distinct()
			.map { date -> EvaluationDateFilter(date) }

	val filters = mutableListOf<EvaluationFilter>()

	filters.addAll(statesFilters)
	filters.addAll(subjectsFilters)
	filters.addAll(datesFilters)

	return filters
}