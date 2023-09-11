package com.gdavidpb.tuindice.evaluations.presentation.mapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationDateFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationStateFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationSubjectFilter
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationFilterSectionItem

@Composable
fun rememberEvaluationFilters(
	availableFilters: List<EvaluationFilter>,
	activeFilters: List<EvaluationFilter>
): List<EvaluationFilterSectionItem> {
	return remember {
		availableFilters.groupBy { filter -> filter::class }
			.map { (clazz, filters) ->
				when (clazz) {
					EvaluationStateFilter::class ->
						EvaluationFilterSectionItem(
							entries = filters.associateWith { filter ->
								mutableStateOf(activeFilters.contains(filter))
							}
						)

					EvaluationSubjectFilter::class ->
						EvaluationFilterSectionItem(
							entries = filters.associateWith { filter ->
								mutableStateOf(activeFilters.contains(filter))
							}
						)

					EvaluationDateFilter::class ->
						EvaluationFilterSectionItem(
							entries = filters.associateWith { filter ->
								mutableStateOf(activeFilters.contains(filter))
							}
						)

					else -> throw NoWhenBranchMatchedException()
				}
			}
	}
}

