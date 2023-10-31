package com.gdavidpb.tuindice.evaluations.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationsViewModel
import com.gdavidpb.tuindice.evaluations.ui.screen.EvaluationsScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun EvaluationsRoute(
	onNavigateToAddEvaluation: () -> Unit,
	onNavigateToEvaluation: (evaluationId: String) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	viewModel: EvaluationsViewModel = koinViewModel()
) {
	val viewState by viewModel.viewState.collectAsStateWithLifecycle()

	CollectEffectWithLifecycle(flow = viewModel.viewEvent) { event ->
		when (event) {
			is Evaluations.Event.NavigateToAddEvaluation ->
				onNavigateToAddEvaluation()

			is Evaluations.Event.NavigateToEvaluation ->
				onNavigateToEvaluation(
					event.evaluationId
				)

			is Evaluations.Event.ShowSnackBar ->
				showSnackBar(
					SnackBarMessage(
						message = event.message
					)
				)
		}
	}

	LaunchedEffect(Unit) {
		viewModel.loadEvaluationsAction()
	}

	EvaluationsScreen(
		state = viewState,
		onAddEvaluationClick = viewModel::addEvaluationAction,
		onEvaluationClick = { /* TODO */ },
		onEvaluationEdit = viewModel::editEvaluationAction,
		onEvaluationDelete = viewModel::removeEvaluationAction,
		onClearFiltersClick = viewModel::loadEvaluationsAction,
		onFilterCheckedChange = viewModel::filterEvaluationsAction,
		onRetryClick = viewModel::loadEvaluationsAction
	)
}