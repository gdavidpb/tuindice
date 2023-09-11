package com.gdavidpb.tuindice.evaluations.presentation.mapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationDateFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationStateFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationSubjectFilter
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationFilterSection

@Composable
fun rememberEvaluationFilters(
	availableFilters: List<EvaluationFilter>,
	activeFilters: List<EvaluationFilter>
): List<EvaluationFilterSection> {
	return remember {
		availableFilters.groupBy { filter -> filter::class }
			.map { (clazz, filters) ->
				when (clazz) {
					EvaluationStateFilter::class ->
						EvaluationFilterSection(
							entries = filters.associateWith { filter ->
								mutableStateOf(activeFilters.contains(filter))
							}
						)

					EvaluationSubjectFilter::class ->
						EvaluationFilterSection(
							entries = filters.associateWith { filter ->
								mutableStateOf(activeFilters.contains(filter))
							}
						)

					EvaluationDateFilter::class ->
						EvaluationFilterSection(
							entries = filters.associateWith { filter ->
								mutableStateOf(activeFilters.contains(filter))
							}
						)

					else -> throw NoWhenBranchMatchedException()
				}
			}
	}
}

