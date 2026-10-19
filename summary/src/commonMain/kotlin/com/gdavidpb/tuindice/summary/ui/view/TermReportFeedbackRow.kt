package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.ui.TermReportUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.term_report_useful_question

// The words that ask for the thumbs, and the two thumbs at the end of the row. The touch area of a thumb is wider than
// its icon, so the row moves out by the margin left around the icon: the icon meets the edge of the content.
@Composable
fun TermReportFeedbackRow(
	feedback: Boolean?,
	onRateClick: (useful: Boolean) -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.testTag(TermReportUiTags.TermReportFeedbackRow),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		Text(
			modifier = Modifier.weight(1f),
			text = stringResource(Res.string.term_report_useful_question),
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)

		Row(
			modifier = Modifier.offset(x = TermReportThumbSizes.TouchMargin),
			horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Small)
		) {
			TermReportThumbButton(useful = true, isSelected = feedback == true, onClick = { onRateClick(true) })
			TermReportThumbButton(useful = false, isSelected = feedback == false, onClick = { onRateClick(false) })
		}
	}
}
