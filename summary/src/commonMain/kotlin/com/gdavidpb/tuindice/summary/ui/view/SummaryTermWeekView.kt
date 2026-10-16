package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.SummaryTermWeekItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

/**
 * The line "Semana 3 de 12" of the term with what is left of it at its end, on the same baseline, and the track of its
 * weeks under it.
 */
@Composable
fun SummaryTermWeekView(week: SummaryTermWeekItem) {
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
