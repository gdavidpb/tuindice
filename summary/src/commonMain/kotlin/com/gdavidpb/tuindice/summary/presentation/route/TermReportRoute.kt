package com.gdavidpb.tuindice.summary.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.summary.presentation.contract.TermReport
import com.gdavidpb.tuindice.summary.presentation.model.TermReportTopBarActionBus
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryNavDependencies
import com.gdavidpb.tuindice.summary.presentation.viewmodel.TermReportViewModel
import com.gdavidpb.tuindice.summary.ui.model.TermReportActions
import com.gdavidpb.tuindice.summary.ui.screen.TermReportScreen

/**
 * The term report, opened on [insightId]. Its share action asks for the sheet that shares its term, which
 * [onNavigateToShareTerm] opens, and its buttons of "Qué sigue" go where the chat sends them, through [navigation].
 */
@Composable
fun TermReportRoute(
	insightId: String,
	navigation: SummaryNavDependencies,
	viewModel: TermReportViewModel,
	topBarActionBus: TermReportTopBarActionBus,
	onNavigateToShareTerm: (termKey: String) -> Unit
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()
	val currentOnNavigateToShareTerm by rememberUpdatedState(onNavigateToShareTerm)

	LaunchedEffect(insightId) {
		viewModel.openAction(insightId = insightId)
	}

	LaunchedEffect(topBarActionBus) {
		topBarActionBus.actions.collect { action ->
			if (action is TopBarAction.TermReportShareAction) viewModel.shareAction()
		}
	}

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is TermReport.Effect.NavigateToShareTerm -> currentOnNavigateToShareTerm(effect.termKey)

			is TermReport.Effect.NavigateToAssistantTarget ->
				navigation.onNavigateToAssistantTarget(effect.target, effect.entityRefs)
		}
	}

	val actions = remember(viewModel) {
		TermReportActions(
			onRateClick = viewModel::rateAction,
			onNextActionClick = viewModel::openNextAction,
			onRetryClick = viewModel::retryAction
		)
	}

	TermReportScreen(
		state = viewState,
		actions = actions
	)
}
