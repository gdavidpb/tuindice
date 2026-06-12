package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius

@Composable
fun SubjectResultCard(
	subjectCode: String,
	nameText: String,
	creditsText: String,
	modifier: Modifier = Modifier,
	containerTestTag: String? = null,
	onClick: (() -> Unit)? = null,
	statusContent: (@Composable () -> Unit)? = null,
	trailingContent: (@Composable () -> Unit)? = null
) {
	val taggedModifier = if (containerTestTag == null) {
		modifier
	} else {
		modifier.testTag(containerTestTag)
	}
	val clickableModifier = if (onClick == null) {
		taggedModifier
	} else {
		taggedModifier
			.clickable(onClick = onClick)
			.semantics(mergeDescendants = false) {}
	}
	Surface(
		modifier = clickableModifier.fillMaxWidth(),
		shape = RoundedCornerShape(TuIndiceRadius.Card),
		color = MaterialTheme.colorScheme.surfaceContainerLow,
		border = BorderStroke(
			width = 1.dp,
			color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f)
		)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 14.dp, vertical = 14.dp),
			horizontalArrangement = Arrangement.spacedBy(12.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Column(modifier = Modifier.weight(1f)) {
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.spacedBy(12.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					SubjectCodeChip(subjectCode = subjectCode)
					Text(
						modifier = Modifier.weight(1f),
						text = nameText,
						style = MaterialTheme.typography.bodyMedium,
						fontWeight = FontWeight.Bold,
						color = MaterialTheme.colorScheme.onSurface,
						maxLines = 2,
						overflow = TextOverflow.Ellipsis
					)
				}
				Spacer(modifier = Modifier.height(8.dp))
				Text(
					text = creditsText,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
				if (statusContent != null) {
					Spacer(modifier = Modifier.height(8.dp))
					statusContent()
				}
			}
			if (trailingContent != null) {
				trailingContent()
			}
		}
	}
}
