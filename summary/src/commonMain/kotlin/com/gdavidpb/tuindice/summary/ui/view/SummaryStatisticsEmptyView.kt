package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryStatisticsEmptyHeading

/**
 * What a block says when it lacks its data: its [heading] when it names what it lacks, with the icon of the screen that
 * has it beside the words, the [message] under the title, and the action that leads to what it lacks when it has one.
 * The same words stand wherever a block is empty, so blocks read alike.
 */
@Composable
fun SummaryStatisticsEmptyView(
	message: String,
	modifier: Modifier = Modifier,
	heading: SummaryStatisticsEmptyHeading? = null,
	actionText: String? = null,
	onActionClick: () -> Unit = {}
) {
	Row(
		modifier = modifier
			.fillMaxWidth()
			.testTag(SummaryUiTags.StatisticsEmpty),
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		heading?.let { head ->
			Icon(
				modifier = Modifier.testTag(SummaryUiTags.StatisticsEmptyIcon),
				imageVector = head.icon,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}

		EmptyWords(
			message = message,
			title = heading?.title,
			actionText = actionText,
			onActionClick = onActionClick
		)
	}
}

@Composable
private fun EmptyWords(
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
