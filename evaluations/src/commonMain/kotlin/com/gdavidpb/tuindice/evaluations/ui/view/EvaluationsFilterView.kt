package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.presentation.mapper.computeEvaluationFilterGroups
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags

@Composable
fun EvaluationFilterView(
	availableFilters: List<EvaluationFilter>,
	activeFilters: List<EvaluationFilter>,
	onFilterCheckedChange: (filter: EvaluationFilter, isChecked: Boolean) -> Unit
) {
	val filters = computeEvaluationFilterGroups(
		availableFilters = availableFilters,
		activeFilters = activeFilters
	)

	LazyColumn(
		modifier = Modifier.testTag(EvaluationsUiTags.EvaluationsFiltersContainer)
	) {
		items(
			items = filters
		) { item ->
			FilterView(
				entries = item.toMap(),
				onCheckedChange = { filter, isChecked ->
					onFilterCheckedChange(filter, isChecked)
				}
			)
		}
	}
}
