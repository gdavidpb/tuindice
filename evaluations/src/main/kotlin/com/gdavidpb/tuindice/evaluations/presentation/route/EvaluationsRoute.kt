package com.gdavidpb.tuindice.evaluations.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationsViewModel
import com.gdavidpb.tuindice.evaluations.ui.screen.EvaluationsScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun EvaluationsRoute(
	onNavigateToAddEvaluation: (title: String) -> Unit,
	onNavigateToEvaluation: (title: String, evaluationId: String) -> Unit,
	showSnackBar: (message: String, actionLabel: String?, action: (() -> Unit)?) -> Unit,
	viewModel: EvaluationsViewModel = koinViewModel()
) {
	val viewState by viewModel.viewState.collectAsStateWithLifecycle()

	CollectEffectWithLifecycle(flow = viewModel.viewEvent) { event ->
		when (event) {
			is Evaluations.Event.NavigateToAddEvaluation ->
				onNavigateToAddEvaluation(
					event.title
				)

			is Evaluations.Event.NavigateToEvaluation ->
				onNavigateToEvaluation(
					event.title,
					event.evaluationId
				)

			is Evaluations.Event.ShowSnackBar ->
				showSnackBar(event.message, null, null)
		}
	}

	LaunchedEffect(Unit) {
		viewModel.loadEvaluationsAction()
	}

	EvaluationsScreen(
		state = viewState,
		onAddEvaluationClick = viewModel::addEvaluationAction,
		onEvaluationClick = viewModel::editEvaluationAction,
		onClearFiltersClick = viewModel::loadEvaluationsAction,
		onFilterCheckedChange = viewModel::filterEvaluationsAction,
		onRetryClick = viewModel::loadEvaluationsAction
	)
}