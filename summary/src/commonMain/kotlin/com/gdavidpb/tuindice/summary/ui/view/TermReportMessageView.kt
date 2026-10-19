package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.ui.TermReportUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.term_report_retry

/**
 * A report that cannot be shown, with what it is about in the title and the message. [onRetryClick] offers to read it
 * again; a report that is not there offers nothing.
 */
@Composable
fun TermReportMessageView(
	title: String,
	message: String,
	tag: String,
	modifier: Modifier = Modifier,
	onRetryClick: (() -> Unit)? = null
) {
	Column(
		modifier = modifier
			.fillMaxSize()
			.testTag(tag)
			.padding(horizontal = TuIndiceSpacing.Screen),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text(
			text = title,
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.onSurface,
			textAlign = TextAlign.Center
		)

		Text(
			modifier = Modifier.padding(top = TuIndiceSpacing.Small),
			text = message,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			textAlign = TextAlign.Center
		)

		if (onRetryClick != null) {
			OutlinedButton(
				modifier = Modifier
					.padding(top = TuIndiceSpacing.Large)
					.testTag(TermReportUiTags.TermReportRetryButton),
				onClick = onRetryClick
			) {
				Text(text = stringResource(Res.string.term_report_retry))
			}
		}
	}
}
