package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

// The words of an empty block: its [title] when it has one, the [message] under it, and the action that leads to what the
// block lacks when it has one.
@Composable
fun SummaryStatisticsEmptyWords(
	message: String,
	title: String?,
	actionText: String?,
	onActionClick: () -> Unit
) {
	Column(verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall)) {
		title?.let { words ->
			Text(
				text = words,
				style = MaterialTheme.typography.bodyLarge,
				fontWeight = FontWeight.Medium,
				color = MaterialTheme.colorScheme.onSurface
			)
		}

		Text(
			text = message,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)

		actionText?.let { label ->
			OutlinedButton(
				modifier = Modifier.testTag(SummaryUiTags.StatisticsEmptyAction),
				onClick = onActionClick
			) {
				Text(text = label)
			}
		}
	}
}
