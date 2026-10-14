package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.presentation.model.SummaryTermItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryTermWeekItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_section_term

/**
 * "Trimestre": which week of the term it is, with the track of its twelve weeks and what is left of them, how many
 * subjects it has and their credits, and the points of each subject. Its link, the name of the term, opens the
 * Record; each subject's row opens the Record on that subject's attempt. Outside weeks 1 to 12 the week, what is
 * left of it and its track are not there, and the subjects remain with their line. With no term in progress the
 * link leads to the Record, the last term that is over comes first and then the invitation to plan the next one, or
 * the first one to come. A term with nothing to show yet says so in a card of its own.
 */
@Composable
fun SummaryTermView(
	term: SummaryTermItem,
	actions: SummaryHubActions,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier
			.fillMaxWidth()
			.testTag(SummaryUiTags.TermSection),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		SummarySectionHeaderView(
			title = stringResource(Res.string.summary_section_term),
			linkText = term.linkText?.asString(),
			onLinkClick = { actions.onTargetClick(SummaryHubTarget.Record) },
			sectionTag = SummaryUiTags.TermSection
		)

		term.finished?.let { finished ->
			SummaryFinishedTermView(
				item = finished,
				onClick = { actions.onTargetClick(SummaryHubTarget.Record) }
			)
		}

		if (term.card == null) {
			TermCardView(term = term, actions = actions)
		}

		term.card?.let { card ->
			SummaryCardView(
				item = card,
				actions = actions,
				modifier = Modifier.testTag(SummaryUiTags.TermCard)
			)
		}
	}
}

// The card of the term in progress with something to show: its week, its summary line and the points of each subject.
@Composable
private fun TermCardView(
	term: SummaryTermItem,
	actions: SummaryHubActions
) {
	val secondary = MaterialTheme.colorScheme.onSurface.copy(alpha = TuIndiceAlpha.Deemphasis)

	SummaryHubCardView {
		Column(
			modifier = Modifier.padding(
				start = TuIndiceSpacing.Screen,
				top = if (term.week != null) TuIndiceSpacing.Screen else TermTopWithoutWeek,
				end = TuIndiceSpacing.Screen,
				// With no rows under them the week and the line are the last thing of the card.
				bottom = if (term.subjects.isEmpty()) TuIndiceSpacing.Screen else TuIndiceSpacing.Small
			),
			verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XLarge)
		) {
			term.week?.let { week -> TermWeekView(week = week) }

			term.summaryText?.let { summary ->
				Text(
					modifier = Modifier.testTag(SummaryUiTags.TermSummary),
					text = summary.asString(),
					style = MaterialTheme.typography.bodyMedium,
					color = secondary
				)
			}
		}

		term.subjects.forEach { subject ->
			SummaryPointsRowView(
				modifier = Modifier.testTag(SummaryUiTags.termPointsRow(subject.subjectCode)),
				item = subject,
				onClick = {
					actions.onTargetClick(SummaryHubTarget.RecordAttempt(attemptId = subject.attemptId))
				}
			)
		}

		if (term.subjects.isNotEmpty()) {
			Spacer(modifier = Modifier.height(TuIndiceSpacing.Medium))
		}
	}
}

// The line "Semana 3 de 12" with what is left of the term at its end, on the same baseline, and the track under it.
@Composable
private fun TermWeekView(week: SummaryTermWeekItem) {
	Column(
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XLarge)
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			SummaryFigureText(
				modifier = Modifier
					.alignByBaseline()
					.testTag(SummaryUiTags.TermWeekText),
				figure = week.valueText.asString(),
				rest = week.restText.asString()
			)

			Text(
				modifier = Modifier
					.alignByBaseline()
					.testTag(SummaryUiTags.TermWeekRemaining),
				text = week.remainingText.asString(),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = TuIndiceAlpha.Deemphasis)
			)
		}

		SummaryWeekTrackView(
			modifier = Modifier.testTag(SummaryUiTags.TermWeekTrack),
			week = week.week,
			totalWeeks = week.totalWeeks,
			description = week.description.asString()
		)
	}
}

// Without a week the card opens with the space it closes with below its last row: the closing spacer and the
// row's own bottom padding, so the first line and the last row keep the same distance from the card's edges.
private val TermTopWithoutWeek = TuIndiceSpacing.Medium + TuIndiceSpacing.Small
