package com.gdavidpb.tuindice.summary.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.ResumedEffect
import com.gdavidpb.tuindice.summary.presentation.contract.SummaryStatistics
import com.gdavidpb.tuindice.summary.presentation.mapper.toSummaryEffect
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryNavDependencies
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryStatisticsViewModel
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions
import com.gdavidpb.tuindice.summary.ui.screen.SummaryStatisticsScreen

/**
 * The Statistics screen, pushed from the hub. Coming back to the front reads the statistics again, and a link that the
 * screen asks for is taken by the same connections the hub uses for the same target.
 */
@Composable
fun SummaryStatisticsRoute(
	navigation: SummaryNavDependencies,
	viewModel: SummaryStatisticsViewModel
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	// The statistics follow the clock, which sleeps with the process: reading them again is the first thing coming back
	// does.
	ResumedEffect(action = viewModel::resumedAction)

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is SummaryStatistics.Effect.NavigateTo -> navigation.leadTo(effect.target.toSummaryEffect())
		}
	}

	val actions = remember(viewModel) {
		SummaryHubActions(onTargetClick = viewModel::openTargetAction)
	}

	SummaryStatisticsScreen(
		state = viewState,
		actions = actions
	)
}
