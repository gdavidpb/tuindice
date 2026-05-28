package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationFilterGroupItem
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags

@Composable
fun EvaluationFilterView(
	groups: List<EvaluationFilterGroupItem>,
	onFilterCheckedChange: (filter: EvaluationFilter, isChecked: Boolean) -> Unit,
	scrollEnabled: Boolean = true
) {
	LazyColumn(
		modifier = Modifier
			.padding(horizontal = 12.dp)
			.testTag(EvaluationsUiTags.EvaluationsFiltersContainer),
		userScrollEnabled = scrollEnabled
	) {
		items(
			items = groups,
			key = ::evaluationFilterGroupKey,
			contentType = { EvaluationFilterGroupContentType }
		) { item ->
			FilterView(
				items = item.items,
				onCheckedChange = { filter, isChecked ->
					onFilterCheckedChange(filter, isChecked)
				}
			)
		}
	}
}

private fun evaluationFilterGroupKey(item: EvaluationFilterGroupItem): String {
	return item.items.joinToString(separator = "|") { chip -> chip.labelText }
}

private const val EvaluationFilterGroupContentType = "evaluation_filter_group"
