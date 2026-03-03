package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter

@Composable
fun FilterView(
	modifier: Modifier = Modifier,
	entries: Map<EvaluationFilter, Boolean>,
	onCheckedChange: (filter: EvaluationFilter, isChecked: Boolean) -> Unit
) {
	LazyRow(
		modifier = modifier
			.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
	) {
		items(
			items = entries.toList(),
			key = { entry -> entry.first.getLabel() }
		) { (filter, isChecked) ->
			FilterChip(
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
