package com.gdavidpb.tuindice.summary.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.InternalScreenDefaults
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.view.EmptyStateAnimationView
import com.gdavidpb.tuindice.base.ui.view.IllustratedMessageView
import com.gdavidpb.tuindice.base.ui.view.LoadingView
import com.gdavidpb.tuindice.summary.presentation.contract.SummaryStatistics
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions
import com.gdavidpb.tuindice.summary.ui.view.SummaryStatisticsCreditsView
import com.gdavidpb.tuindice.summary.ui.view.SummaryStatisticsCurrentTermView
import com.gdavidpb.tuindice.summary.ui.view.SummaryStatisticsGradesView
import com.gdavidpb.tuindice.summary.ui.view.SummaryStatisticsIndexView
import com.gdavidpb.tuindice.summary.ui.view.SummaryStatisticsPaceView
import com.gdavidpb.tuindice.summary.ui.view.SummaryStatisticsSubjectsView

/**
 * The Statistics screen: a progress indicator while the statistics are read, the message that says why they cannot be
 * read, or else the six blocks in their order, scrolling under the top bar. The links and the actions of the blocks go
 * through [actions], to the screens that own what they say.
 */
@Composable
fun SummaryStatisticsScreen(
	state: SummaryStatistics.State,
	actions: SummaryHubActions,
	modifier: Modifier = Modifier
) {
	Box(
		modifier = modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
	) {
		when (state) {
			SummaryStatistics.State.Idle -> Unit

			SummaryStatistics.State.Loading -> LoadingView(indicatorTag = SummaryUiTags.LoadingIndicator)

			is SummaryStatistics.State.Content -> SummaryStatisticsContent(
				statistics = state.statistics,
				actions = actions
			)

			is SummaryStatistics.State.Unavailable -> SummaryStatisticsUnavailable(
				title = state.title.asString(),
				message = state.message.asString()
			)
		}
	}
}

@Composable
private fun SummaryStatisticsContent(
	statistics: SummaryStatisticsItem,
	actions: SummaryHubActions
) {
	val chartActionLabel = statistics.chartActionLabel.asString()

	Column(
		modifier = Modifier
			.testTag(SummaryUiTags.StatisticsContent)
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
		SummaryStatisticsCurrentTermView(
			item = statistics.currentTerm,
			chartActionLabel = chartActionLabel,
			actions = actions
		)

		SummaryStatisticsIndexView(
			item = statistics.index,
			chartActionLabel = chartActionLabel
		)

		SummaryStatisticsSubjectsView(
			item = statistics.subjects,
			chartActionLabel = chartActionLabel
		)

		SummaryStatisticsCreditsView(
			item = statistics.credits,
			chartActionLabel = chartActionLabel
		)

		SummaryStatisticsGradesView(
			item = statistics.grades,
			chartActionLabel = chartActionLabel
		)

		SummaryStatisticsPaceView(
			item = statistics.pace,
			chartActionLabel = chartActionLabel
		)
	}
}

// The record is not ready to be read: the screen says so and offers nothing to retry, since it does not retry.
@Composable
private fun SummaryStatisticsUnavailable(
	title: String,
	message: String
) {
	IllustratedMessageView(
		modifier = Modifier
			.testTag(SummaryUiTags.StatisticsUnavailable)
			.fillMaxSize()
			.padding(
				start = TuIndiceSpacing.Screen,
				top = InternalScreenDefaults.TopBarSpacing + TuIndiceSpacing.Dialog,
				end = TuIndiceSpacing.Screen,
				bottom = TuIndiceSpacing.Dialog
			),
		title = title,
		message = message,
		titleTestTag = SummaryUiTags.StatisticsUnavailableTitle,
		messageTestTag = SummaryUiTags.StatisticsUnavailableMessage,
		titleStyle = MaterialTheme.typography.headlineSmall,
		headerContent = { EmptyStateAnimationView() }
	)
}
