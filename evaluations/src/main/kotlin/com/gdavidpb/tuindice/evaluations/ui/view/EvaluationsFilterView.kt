package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationFilterSectionItem

@Composable
fun EvaluationFilterView(
	filters: List<EvaluationFilterSectionItem>,
	onFilterApplied: (activeFilters: List<EvaluationFilter>) -> Unit
) {
	LazyColumn {
		items(filters) { item ->
			val scrollState = rememberScrollState()

			FilterView(
				modifier = Modifier
					.padding(horizontal = dimensionResource(id = R.dimen.dp_12))
					.horizontalScroll(scrollState),
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