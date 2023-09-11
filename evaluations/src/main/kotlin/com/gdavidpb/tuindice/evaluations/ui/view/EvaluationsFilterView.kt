package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationFilterSection

@Composable
fun EvaluationFilterView(
	filters: List<EvaluationFilterSection>,
	onFilterApplied: (activeFilters: List<EvaluationFilter>) -> Unit
) {
	LazyColumn {
		items(filters) { item ->
			FilterView(
				modifier = Modifier
					.padding(horizontal = dimensionResource(id = R.dimen.dp_12)),
				entries = item.entries,
				onEntrySelect = { filter, isSelected ->
					item.entries[filter]?.value = isSelected

					val activeFilters = filters
						.flatMap { item ->
							item
								.entries
								.entries
								.mapNotNull { (filter, state) ->
									if (state.value) filter else null
								}
						}

					onFilterApplied(activeFilters)
				}
			)
		}
	}
}