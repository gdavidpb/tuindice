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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationAttemptPickerItem
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags

@Composable
fun EvaluationAttemptPicker(
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
	items: List<EvaluationAttemptPickerItem>,
	onAttemptChange: (attempt: EditableAttemptDescriptor?) -> Unit
) {
	FlowRow(
		modifier = modifier
			.animateContentSize(animationSpec = spring())
			.testTag(EvaluationsUiTags.EvaluationSubjectPickerRow)
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
					modifier = Modifier.testTag(
						EvaluationsUiTags.evaluationSubjectChip(item.attempt.id)
					),
					selected = item.isSelected,
					enabled = enabled,
					colors = FilterChipDefaults.filterChipColors(
						containerColor = item.containerColor,
						labelColor = item.contentColor,
						disabledContainerColor = item.disabledContainerColor,
						disabledLabelColor = item.disabledContentColor,
						selectedContainerColor = item.containerColor,
						selectedLabelColor = item.contentColor
					),
					onClick = {
						if (item.isSelected) {
							onAttemptChange(null)
						} else {
							onAttemptChange(item.attempt)
						}
					},
					label = {
						Text(
							text = item.labelText,
							fontWeight = if (item.isSelected) {
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
}
