package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.TermReportSubjectsItem
import com.gdavidpb.tuindice.summary.ui.TermReportUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.term_report_subjects_title

/**
 * The subjects of the term: each with its code, its name and its grade in a badge coloured by what the term says of it,
 * then the count of each outcome and the credits the term approved.
 */
@Composable
fun TermReportSubjectsView(
	subjects: TermReportSubjectsItem,
	modifier: Modifier = Modifier
) {
	// The heading and the credits are outside the card, as the other blocks of the report are; the subjects are in it.
	Column(
		modifier = modifier
			.fillMaxWidth()
			.testTag(TermReportUiTags.TermReportSubjects),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
		) {
			Text(
				text = stringResource(Res.string.term_report_subjects_title),
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onSurface
			)

			Spacer(modifier = Modifier.weight(1f))

			Text(
				modifier = Modifier.testTag(TermReportUiTags.TermReportCredits),
				text = subjects.credits.asString(),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}

		SummaryHubCardView {
			Column(
				modifier = Modifier.padding(TuIndiceSpacing.Screen),
				verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
			) {
				subjects.rows.forEachIndexed { index, row ->
					if (index > 0) {
						HorizontalDivider(
							thickness = TuIndiceSpacing.Hairline,
							color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = TuIndiceAlpha.Deemphasis)
						)
					}

					TermReportSubjectRow(row = row)
				}

				TermReportLegendView(legend = subjects.legend)
			}
		}
	}
}
