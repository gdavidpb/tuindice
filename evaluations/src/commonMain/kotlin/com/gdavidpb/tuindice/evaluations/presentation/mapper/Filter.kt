package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter

fun computeEvaluationFilterGroups(
	availableFilters: List<EvaluationFilter>,
	activeFilters: List<EvaluationFilter>
): List<MutableMap<EvaluationFilter, Boolean>> {
	return availableFilters
		.groupBy { filter -> filter::class }
		.map { (_, filters) ->
			filters
				.associateWith { filter -> activeFilters.contains(filter) }
				.toMutableMap()
		}
		.toList()
}