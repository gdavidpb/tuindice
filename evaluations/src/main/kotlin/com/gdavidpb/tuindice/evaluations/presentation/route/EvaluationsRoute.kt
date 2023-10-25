package com.gdavidpb.tuindice.evaluations.presentation.route

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.model.rememberDialogState
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsDialog
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationsViewModel
import com.gdavidpb.tuindice.evaluations.ui.dialog.RemoveEvaluationDialog
import com.gdavidpb.tuindice.evaluations.ui.screen.EvaluationsScreen
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluationsRoute(
	onNavigateToAddEvaluation: () -> Unit,
	onNavigateToEvaluation: (evaluationId: String) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	viewModel: EvaluationsViewModel = koinViewModel()
) {
	val viewState by viewModel.viewState.collectAsStateWithLifecycle()

	val sheetState = rememberModalBottomSheetState()
	val dialogState = rememberDialogState<EvaluationsDialog>()

	CollectEffectWithLifecycle(flow = viewModel.viewEvent) { event ->
		when (event) {
			is Evaluations.Event.NavigateToAddEvaluation ->
				onNavigateToAddEvaluation()

			is Evaluations.Event.NavigateToEvaluation ->
				onNavigateToEvaluation(
					event.evaluationId
				)

			is Evaluations.Event.ShowSnackBar ->
				showSnackBar(SnackBarMessage(message = event.message))

			is Evaluations.Event.ShowRemoveEvaluationDialog ->
				dialogState.value = EvaluationsDialog.RemoveEvaluationDialog(
					evaluationId = event.evaluationId,
					message = event.message
				)

			is Evaluations.Event.CloseDialog ->
				dialogState.value = null
		}
	}

	LaunchedEffect(Unit) {
		viewModel.loadEvaluationsAction()
	}

	when (val state = dialogState.value) {
		is EvaluationsDialog.RemoveEvaluationDialog ->
			RemoveEvaluationDialog(
				sheetState = sheetState,
				evaluationId = state.evaluationId,
				onConfirmClick = viewModel::confirmRemoveEvaluationAction,
				onDismissRequest = viewModel::closeDialogAction
			)

		null -> {}
	}

	EvaluationsScreen(
		state = viewState,
		onAddEvaluationClick = viewModel::addEvaluationAction,
		onEvaluationClick = viewModel::editEvaluationAction,
		onEvaluationDelete = viewModel::removeEvaluationAction,
		onEvaluationIsCompletedChange = viewModel::updateEvaluationIsCompletedAction,
		onClearFiltersClick = viewModel::loadEvaluationsAction,
		onFilterCheckedChange = viewModel::filterEvaluationsAction,
		onRetryClick = viewModel::loadEvaluationsAction
	)
}