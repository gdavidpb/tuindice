package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags

@Composable
fun FilterView(
	modifier: Modifier = Modifier,
	entries: Map<EvaluationFilter, Boolean>,
	onCheckedChange: (filter: EvaluationFilter, isChecked: Boolean) -> Unit
) {
	LazyRow(
		modifier = modifier
			.testTag(EvaluationsUiTags.EvaluationsFilterRow)
			.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
	) {
		itemsIndexed(
			items = entries.toList(),
			key = { _, entry -> entry.first.getLabel() }
		) { _, (filter, isChecked) ->
			FilterChip(
				modifier = Modifier.testTag(EvaluationsUiTags.filterChip(filter.getLabel())),
				selected = isChecked,
				onClick = {
					onCheckedChange(filter, !isChecked)
				},
				leadingIcon = if (isChecked) {
					{ Text(text = "\u2713") }
				} else null,
				label = {
					Text(
						text = filter.getLabel(),
						maxLines = 1
					)
				}
			)
		}
	}
}
