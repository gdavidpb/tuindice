package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.presentation.mapper.computeEvaluationFilterGroups

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

	LazyColumn {
		items(filters) { item ->
			FilterView(
				modifier = Modifier
					.padding(horizontal = dimensionResource(id = R.dimen.dp_12)),
				entries = item.toMap(),
				onCheckedChange = { filter, isChecked ->
					onFilterCheckedChange(filter, isChecked)
				}
			)
		}
	}
}