package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekItem
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags

@Composable
fun EvaluationWeekSelectorItemView(
	item: EvaluationsWeekItem,
	isSelected: Boolean,
	showCurrentIndicator: Boolean = true,
	modifier: Modifier = Modifier
) {
	Row(
		modifier = modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.Center,
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			modifier = Modifier
				.weight(1f, fill = false)
				.testTag(EvaluationsUiTags.EvaluationsWeekLabel),
			text = item.labelText,
			style = MaterialTheme.typography.titleLarge,
			fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
			color = if (isSelected) {
				MaterialTheme.colorScheme.onSurface
			} else {
				MaterialTheme.colorScheme.onSurfaceVariant
			},
			textAlign = TextAlign.Center,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis
		)

		if (showCurrentIndicator && item.isCurrent) {
			Box(
				modifier = Modifier
					.padding(start = 8.dp)
					.size(8.dp)
					.clip(CircleShape)
					.background(MaterialTheme.colorScheme.secondary)
			)
		}
	}
}
