package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.record.ui.RecordUiTags

@Composable
fun QualitativeStatusSelector(
	modifier: Modifier = Modifier,
	attemptId: String,
	selectedItem: QualitativeStatusDropdownItem?,
	placeholderText: String,
	metadataText: String?,
	items: List<QualitativeStatusDropdownItem>,
	onSelected: (AttemptOutcome) -> Unit
) {
	val expanded = remember { mutableStateOf(false) }

	Column(
		modifier = modifier,
		horizontalAlignment = Alignment.End
	) {
		Row(
			modifier = Modifier
				.testTag(RecordUiTags.attemptStatusSelector(attemptId))
				.clickable { expanded.value = !expanded.value }
				.padding(vertical = 2.dp),
			horizontalArrangement = Arrangement.spacedBy(6.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			AttemptStatusBadge(
				modifier = selectedItem?.let { item ->
					Modifier.testTag(
						RecordUiTags.attemptStatusValue(
							attemptId = attemptId,
							status = item.outcome.name.lowercase()
						)
					)
				} ?: Modifier,
				text = selectedItem?.label ?: placeholderText,
				contentColor = if (selectedItem == null) {
					MaterialTheme.colorScheme.onSurfaceVariant
				} else {
					MaterialTheme.colorScheme.onSurface
				}
			)
			Text(
				text = "v",
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				style = MaterialTheme.typography.labelLarge
			)
		}

		AnimatedVisibility(
			visible = expanded.value,
			enter = fadeIn() + expandVertically(),
			exit = fadeOut() + shrinkVertically()
		) {
			Surface(
				modifier = Modifier
					.padding(top = 6.dp),
				shape = RoundedCornerShape(TuIndiceRadius.Medium),
				color = MaterialTheme.colorScheme.surfaceContainerHigh
			) {
				Column(
					modifier = Modifier.padding(vertical = 4.dp)
				) {
					items.forEach { item ->
						Text(
							modifier = Modifier
								.testTag(
									RecordUiTags.attemptStatusOption(
										attemptId = attemptId,
										status = item.outcome.name.lowercase()
									)
								)
								.clickable {
									onSelected(item.outcome)
									expanded.value = false
								}
								.padding(horizontal = 12.dp, vertical = 8.dp),
							text = item.label,
							color = MaterialTheme.colorScheme.onSurface,
							style = MaterialTheme.typography.labelLarge
						)
					}
				}
			}
		}

		if (metadataText != null) {
			AttemptStatusBadge(
				modifier = Modifier.padding(top = 4.dp),
				text = metadataText
			)
		}
	}
}
