package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.presentation.model.SummaryWeekItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_link_schedule
import tuindice.summary.generated.resources.summary_section_week

/**
 * "Esta semana": the class in progress or next and the evaluations that have not gone by, in one
 * card; its link opens the Schedule sheet.
 */
@Composable
fun SummaryWeekView(
	week: SummaryWeekItem,
	actions: SummaryHubActions,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier
			.fillMaxWidth()
			.testTag(SummaryUiTags.WeekSection),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		SummarySectionHeaderView(
			title = stringResource(Res.string.summary_section_week),
			linkText = stringResource(Res.string.summary_link_schedule),
			onLinkClick = { actions.onTargetClick(SummaryHubTarget.Schedule) },
			sectionTag = SummaryUiTags.WeekSection
		)

		SummaryHubCardView {
			week.classRow?.let { classRow ->
				SummaryClassRowView(
					modifier = Modifier.testTag(SummaryUiTags.WeekClassRow),
					item = classRow,
					onClick = { actions.onTargetClick(SummaryHubTarget.Schedule) }
				)
			}

			week.evaluations.forEachIndexed { index, evaluation ->
				if (index > 0 || week.classRow != null) {
					SummaryHubDividerView()
				}

				SummaryEvaluationRowView(
					modifier = Modifier.testTag(SummaryUiTags.weekEvaluationRow(evaluation.evaluationId)),
					item = evaluation,
					onClick = {
						actions.onTargetClick(SummaryHubTarget.EvaluationsWeek(weekStart = evaluation.weekStart))
					}
				)
			}
		}
	}
}
