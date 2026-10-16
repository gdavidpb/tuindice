package com.gdavidpb.tuindice.summary.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.view.LoadingView
import com.gdavidpb.tuindice.summary.presentation.contract.SummaryStatistics
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions
import com.gdavidpb.tuindice.summary.ui.view.SummaryStatisticsContent
import com.gdavidpb.tuindice.summary.ui.view.SummaryStatisticsUnavailable

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
