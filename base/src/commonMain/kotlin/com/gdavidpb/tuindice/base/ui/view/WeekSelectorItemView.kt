package com.gdavidpb.tuindice.base.ui.view

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
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.model.WeekSelectorTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// One week of the strip: its label, in bold and the strong tone when it is the selected week, and a dot after it when
// the week holds today. The tags name the label and the dot for the screen that shows the strip.
@Composable
fun WeekSelectorItemView(
	labelText: UiText,
	isCurrent: Boolean,
	isSelected: Boolean,
	tags: WeekSelectorTags,
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
				.testTag(tags.label),
			text = labelText.asString(),
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

		if (isCurrent) {
			Box(
				modifier = Modifier
					.testTag(tags.currentIndicator)
					.padding(start = TuIndiceSpacing.Medium)
					.size(8.dp)
					.clip(CircleShape)
					.background(MaterialTheme.colorScheme.secondary)
			)
		}
	}
}
