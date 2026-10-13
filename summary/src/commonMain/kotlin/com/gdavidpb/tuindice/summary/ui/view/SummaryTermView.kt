package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.presentation.model.SummaryTermItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_section_term

/**
 * "Trimestre": which week of the term it is, with the track of its twelve weeks, and the points of
 * each subject. Its link, the name of the term, opens the Record. Outside weeks 1 to 12 the week and
 * its track are not there and only the points remain.
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
			linkText = term.nameText.asString(),
			onLinkClick = { actions.onTargetClick(SummaryHubTarget.Record) },
			sectionTag = SummaryUiTags.TermSection
		)

		SummaryHubCardView {
			term.week?.let { week ->
				Column(
					modifier = Modifier.padding(
						start = TuIndiceSpacing.Screen,
						top = TuIndiceSpacing.Screen,
						end = TuIndiceSpacing.Screen,
						bottom = TuIndiceSpacing.Medium
					),
					verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XLarge)
				) {
					SummaryFigureText(
						modifier = Modifier.testTag(SummaryUiTags.TermWeekText),
						figure = week.valueText.asString(),
						rest = week.restText.asString()
					)

					SummaryWeekTrackView(
						modifier = Modifier.testTag(SummaryUiTags.TermWeekTrack),
						week = week.week,
						totalWeeks = week.totalWeeks,
						description = week.description.asString()
					)
				}
			}

			term.subjects.forEach { subject ->
				SummaryPointsRowView(
					modifier = Modifier.testTag(SummaryUiTags.termPointsRow(subject.subjectCode)),
					item = subject
				)
			}

			if (term.subjects.isNotEmpty()) {
				Spacer(modifier = Modifier.height(TuIndiceSpacing.Medium))
			}
		}
	}
}
