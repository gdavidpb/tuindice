package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.model.SubjectCodeChipVariant
import com.gdavidpb.tuindice.base.ui.style.AcademicStatusColors
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.view.SubjectCodeChip
import com.gdavidpb.tuindice.summary.domain.model.SummaryTermReportOutcome
import com.gdavidpb.tuindice.summary.presentation.model.TermReportLegendItem
import com.gdavidpb.tuindice.summary.presentation.model.TermReportSubjectItem
import com.gdavidpb.tuindice.summary.presentation.model.TermReportSubjectsItem
import com.gdavidpb.tuindice.summary.ui.TermReportUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.term_report_subjects_no_grade
import tuindice.summary.generated.resources.term_report_subjects_title

/**
 * The subjects of the term: each with its code, its name and its grade in a badge coloured by what the term says of it,
 * then the count of each outcome and the credits the term approved.
 */
// The heading and the credits are outside the card, as the other blocks of the report are; the subjects are in it.
@Composable
fun TermReportSubjectsView(
	subjects: TermReportSubjectsItem,
	modifier: Modifier = Modifier
) {
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

@Composable
private fun TermReportSubjectRow(
	row: TermReportSubjectItem,
	modifier: Modifier = Modifier
) {
	Row(
		modifier = modifier
			.fillMaxWidth()
			.testTag(TermReportUiTags.termReportSubject(row.subjectCode)),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		SubjectCodeChip(
			subjectCode = row.subjectCode,
			variant = SubjectCodeChipVariant.Dense
		)

		Text(
			modifier = Modifier.weight(1f),
			text = row.subjectName,
			style = MaterialTheme.typography.bodyLarge,
			color = MaterialTheme.colorScheme.onSurface
		)

		TermReportGradeBadge(grade = row.grade, outcome = row.outcome)
	}
}

// The grade in a disc of the colour of its outcome. A subject with no grade yet shows a dash in the neutral colour.
@Composable
private fun TermReportGradeBadge(
	grade: String?,
	outcome: SummaryTermReportOutcome,
	modifier: Modifier = Modifier
) {
	val color = outcome.color()

	Box(
		modifier = modifier
			.size(GradeBadgeSize)
			.background(color = color.copy(alpha = BADGE_ALPHA), shape = CircleShape),
		contentAlignment = Alignment.Center
	) {
		Text(
			text = grade ?: stringResource(Res.string.term_report_subjects_no_grade),
			style = MaterialTheme.typography.titleSmall,
			fontWeight = FontWeight.Bold,
			color = color
		)
	}
}

// The count of each outcome, each in its colour, in the order the screen lists them.
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TermReportLegendView(
	legend: List<TermReportLegendItem>,
	modifier: Modifier = Modifier
) {
	FlowRow(
		modifier = modifier
			.fillMaxWidth()
			.testTag(TermReportUiTags.TermReportLegend),
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Large),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall)
	) {
		legend.forEach { item ->
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall)
			) {
				Box(
					modifier = Modifier
						.size(LegendDotSize)
						.background(color = item.outcome.color(), shape = CircleShape)
				)

				Text(
					text = item.text.asString(),
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}
	}
}

// The colour of an outcome: the approved with the approval colour, the failed with the error colour, the withdrawn with
// the available colour as the record marks a withdrawal, and the pending in the neutral one.
@Composable
fun SummaryTermReportOutcome.color(): Color = when (this) {
	SummaryTermReportOutcome.APPROVED -> AcademicStatusColors.approved()
	SummaryTermReportOutcome.FAILED -> MaterialTheme.colorScheme.error
	SummaryTermReportOutcome.WITHDRAWN -> AcademicStatusColors.available()
	SummaryTermReportOutcome.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
}

private val GradeBadgeSize = 32.dp
private val LegendDotSize = 8.dp
private const val BADGE_ALPHA = 0.16f
