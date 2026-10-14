package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

/**
 * What a block says when it lacks its data: a [title] in bold when it has one, the [message] under it, and the action
 * that leads to what it lacks when it has one. The same words stand wherever a block is empty, so blocks read alike.
 */
@Composable
fun SummaryStatisticsEmptyView(
	message: String,
	modifier: Modifier = Modifier,
	title: String? = null,
	actionText: String? = null,
	onActionClick: () -> Unit = {}
) {
	Column(
		modifier = modifier
			.fillMaxWidth()
			.testTag(SummaryUiTags.StatisticsEmpty),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall)
	) {
		title?.let { heading ->
			Text(
				text = heading,
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
