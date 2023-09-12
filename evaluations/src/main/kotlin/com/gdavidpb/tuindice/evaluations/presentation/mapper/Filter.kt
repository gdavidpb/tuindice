package com.gdavidpb.tuindice.evaluations.presentation.mapper

import androidx.compose.runtime.mutableStateOf
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationDateFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationStateFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationSubjectFilter
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationFilterGroup

fun computeEvaluationFilters(
	availableFilters: List<EvaluationFilter>,
	activeFilters: List<EvaluationFilter>
): List<EvaluationFilterGroup> {
	return availableFilters.groupBy { filter -> filter::class }
		.map { (clazz, filters) ->
			when (clazz) {
				EvaluationStateFilter::class ->
					EvaluationFilterGroup(
						entries = filters.associateWith { filter ->
							mutableStateOf(activeFilters.contains(filter))
						}
					)

				EvaluationSubjectFilter::class ->
					EvaluationFilterGroup(
						entries = filters.associateWith { filter ->
							mutableStateOf(activeFilters.contains(filter))
						}
					)

				EvaluationDateFilter::class ->
					EvaluationFilterGroup(
						entries = filters.associateWith { filter ->
							mutableStateOf(activeFilters.contains(filter))
						}
					)

				else -> throw NoWhenBranchMatchedException()
			}
		}
}

