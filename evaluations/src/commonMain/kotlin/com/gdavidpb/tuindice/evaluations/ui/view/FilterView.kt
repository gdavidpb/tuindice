package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationFilterChipItem
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags

@Composable
fun FilterView(
	modifier: Modifier = Modifier,
	items: List<EvaluationFilterChipItem>,
	onCheckedChange: (filter: EvaluationFilter, isChecked: Boolean) -> Unit
) {
	LazyRow(
		modifier = modifier
			.testTag(EvaluationsUiTags.EvaluationsFilterRow)
			.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
	) {
		itemsIndexed(
			items = items,
			key = { _, item -> item.labelText }
		) { _, item ->
			FilterChip(
				modifier = Modifier.testTag(EvaluationsUiTags.filterChip(item.labelText)),
				selected = item.isChecked,
				colors = if (
					(item.containerColor != null) &&
					(item.contentColor != null)
				) {
					FilterChipDefaults.filterChipColors(
						containerColor = item.containerColor,
						labelColor = item.contentColor,
						selectedContainerColor = item.containerColor,
						selectedLabelColor = item.contentColor
					)
				} else {
					FilterChipDefaults.filterChipColors()
				},
				onClick = {
					onCheckedChange(item.filter, !item.isChecked)
				},
				leadingIcon = if (item.isChecked) {
					{
						Text(
							modifier = Modifier.testTag(
								EvaluationsUiTags.filterChipCheck(item.labelText)
							),
							text = "\u2713",
							color = item.contentColor ?: Color.Unspecified
						)
					}
				} else null,
				label = {
					Text(
						text = item.labelText,
						fontWeight = if (item.emphasizeWhenChecked && item.isChecked) {
							FontWeight.SemiBold
						} else {
							FontWeight.Medium
						},
						maxLines = 1
					)
				}
			)
		}
	}
}
