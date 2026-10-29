package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.TermReportPaceItem
import com.gdavidpb.tuindice.summary.ui.TermReportUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.term_report_pace_title
import tuindice.summary.generated.resources.term_report_pace_unit

/**
 * "Tu ritmo": the credits each regular term approved as a bar, the term of the report in the accent, the average of the
 * regular terms as a dashed line, and under the chart what the term approved beside that average.
 */
@Composable
fun TermReportPaceView(
	pace: TermReportPaceItem,
	chartActionLabel: String,
	modifier: Modifier = Modifier
) {
	val title = stringResource(Res.string.term_report_pace_title)

	Column(
		modifier = modifier
			.fillMaxWidth()
			.testTag(TermReportUiTags.TermReportPace),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
		) {
			Text(
				text = title,
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onSurface
			)

			Spacer(modifier = Modifier.weight(1f))

			Text(
				text = stringResource(Res.string.term_report_pace_unit),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}

		SummaryHubCardView {
			Column(
				modifier = Modifier.padding(TuIndiceSpacing.Screen),
				verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XLarge)
			) {
				when (pace) {
					is TermReportPaceItem.Empty -> Text(
						modifier = Modifier.testTag(TermReportUiTags.TermReportPaceEmpty),
						text = pace.text.asString(),
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)

					is TermReportPaceItem.Data -> TermReportPaceData(
						pace = pace,
						title = title,
						chartActionLabel = chartActionLabel
					)
				}
			}
		}
	}
}
