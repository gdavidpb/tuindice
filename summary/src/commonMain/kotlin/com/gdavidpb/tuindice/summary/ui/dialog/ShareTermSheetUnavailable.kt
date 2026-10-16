package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

/**
 * The sheet when the statistics cannot be read: it says why there is nothing to share, in [title] and [message], and
 * offers nothing to retry.
 */
@Composable
fun ShareTermSheetUnavailable(
	title: String,
	message: String
) {
	Column(
		modifier = Modifier
			.testTag(SummaryUiTags.ShareTermUnavailable)
			.fillMaxWidth(),
		verticalArrangement = Arrangement.spacedBy(8.dp)
	) {
		Text(
			modifier = Modifier.testTag(SummaryUiTags.ShareTermUnavailableTitle),
			text = title,
			style = MaterialTheme.typography.titleMedium
		)

		Text(
			modifier = Modifier.testTag(SummaryUiTags.ShareTermUnavailableMessage),
			text = message,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}
