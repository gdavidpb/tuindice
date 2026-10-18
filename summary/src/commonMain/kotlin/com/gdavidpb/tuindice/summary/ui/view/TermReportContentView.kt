package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.style.InternalScreenDefaults
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.TermReportItem
import com.gdavidpb.tuindice.summary.ui.TermReportUiTags
import com.gdavidpb.tuindice.summary.ui.model.TermReportActions
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_statistics_chart_action

/**
 * The report, top to bottom: the figures of its term, its subjects and its pace, then what the assistant wrote, and the
 * actions that follow it. The screen scrolls under its top bar. [onAskAssistantClick] is null when the assistant cannot
 * be asked, and the question is then not offered.
 */
@Composable
fun TermReportContentView(
	report: TermReportItem,
	feedback: Boolean?,
	onAskAssistantClick: (() -> Unit)?,
	actions: TermReportActions,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier
			.testTag(TermReportUiTags.TermReportContent)
			.fillMaxSize()
			.verticalScroll(rememberScrollState())
			.padding(
				start = TuIndiceSpacing.Screen,
				top = InternalScreenDefaults.TopBarSpacing + TuIndiceSpacing.Medium,
				end = TuIndiceSpacing.Screen,
				bottom = TuIndiceSpacing.Wide
			),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Screen)
	) {
		TermReportHeroView(hero = report.hero)

		TermReportSubjectsView(subjects = report.subjects)

		TermReportPaceView(
			pace = report.pace,
			chartActionLabel = stringResource(Res.string.summary_statistics_chart_action)
		)

		TermReportAssistantView(
			report = report,
			feedback = feedback,
			onAskAssistantClick = onAskAssistantClick,
			onRateClick = actions.onRateClick
		)

		TermReportNextActionsView(
			actions = report.nextActions,
			onActionClick = actions.onNextActionClick
		)
	}
}
