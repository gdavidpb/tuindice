package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.SubjectColorGenerator
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationSubjectFilter
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
			val subjectColors = (filter as? EvaluationSubjectFilter)?.let {
				remember(filter.getLabel()) {
					SubjectColorGenerator.fromCode(filter.getLabel())
				}
			}

			FilterChip(
				modifier = Modifier.testTag(EvaluationsUiTags.filterChip(filter.getLabel())),
				selected = isChecked,
				colors = subjectColors?.let { colors ->
					FilterChipDefaults.filterChipColors(
						containerColor = colors.containerColor,
						labelColor = colors.color,
						selectedContainerColor = colors.containerColor,
						selectedLabelColor = colors.color
					)
				} ?: FilterChipDefaults.filterChipColors(),
				onClick = {
					onCheckedChange(filter, !isChecked)
				},
				leadingIcon = if (isChecked) {
					{
						Text(
							modifier = Modifier.testTag(
								EvaluationsUiTags.filterChipCheck(filter.getLabel())
							),
							text = "\u2713",
							color = subjectColors?.color ?: Color.Unspecified
						)
					}
				} else null,
				label = {
					Text(
						text = filter.getLabel(),
						fontWeight = if (subjectColors != null && isChecked) {
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
