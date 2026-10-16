package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions

/**
 * The card of the term in progress with something to show: its week with the track of the term, its summary line and
 * the points of each subject, each row of which opens the Record on its attempt.
 */
@Composable
fun SummaryTermCardView(
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
			term.week?.let { week -> SummaryTermWeekView(week = week) }

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

// Without a week the card opens with the space it closes with below its last row: the closing spacer and the row's own
// bottom padding, so the first line and the last row keep the same distance from the card's edges.
private val TermTopWithoutWeek = TuIndiceSpacing.Medium + TuIndiceSpacing.Small
