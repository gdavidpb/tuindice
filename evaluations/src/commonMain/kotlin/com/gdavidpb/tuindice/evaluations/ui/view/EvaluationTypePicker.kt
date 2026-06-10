package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.core.spring
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationTypePickerItem
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags

@Composable
fun EvaluationTypePicker(
	items: List<EvaluationTypePickerItem>,
	onTypeChange: (EvaluationType?) -> Unit
) {
	val containerColor = MaterialTheme.colorScheme.surfaceContainerLow
	val selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)

	FlowRow(
		modifier = Modifier
			.animateContentSize(animationSpec = spring())
			.testTag(EvaluationsUiTags.EvaluationTypePickerRow)
			.padding(top = 8.dp)
			.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(6.dp)
	) {
		items.forEach { item ->
			AnimatedVisibility(
				visible = item.isVisible,
				enter = fadeIn() + expandHorizontally(animationSpec = spring()),
				exit = fadeOut() + shrinkHorizontally(animationSpec = spring())
			) {
				FilterChip(
					modifier = Modifier.testTag(EvaluationsUiTags.evaluationTypeChip(item.type.name)),
					selected = item.isSelected,
					colors = FilterChipDefaults.filterChipColors(
						containerColor = containerColor,
						labelColor = MaterialTheme.colorScheme.onSurface,
						selectedContainerColor = selectedContainerColor,
						selectedLabelColor = MaterialTheme.colorScheme.onSurface
					),
					onClick = {
						if (item.isSelected) {
							onTypeChange(null)
						} else {
							onTypeChange(item.type)
						}
					},
					leadingIcon = {
						Icon(
							imageVector = item.icon,
							tint = if (item.isSelected) {
								MaterialTheme.colorScheme.primary
							} else {
								MaterialTheme.colorScheme.outline
							},
							contentDescription = null
						)
					},
					label = {
						Text(
							text = item.labelText,
							maxLines = 1
						)
					}
				)
			}
		}
	}
}
