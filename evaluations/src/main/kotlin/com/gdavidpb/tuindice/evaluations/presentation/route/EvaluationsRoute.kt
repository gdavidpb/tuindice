package com.gdavidpb.tuindice.evaluations.presentation.route

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.model.rememberDialogState
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsDialog
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationsViewModel
import com.gdavidpb.tuindice.evaluations.ui.dialog.FilterDialog
import com.gdavidpb.tuindice.evaluations.ui.screen.EvaluationsScreen
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluationsRoute(
	showSnackBar: (message: String, actionLabel: String?, action: (() -> Unit)?) -> Unit,
	viewModel: EvaluationsViewModel = koinViewModel()
) {
	val viewState by viewModel.viewState.collectAsStateWithLifecycle()

	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
	val dialogState = rememberDialogState<EvaluationsDialog>()

	CollectEffectWithLifecycle(flow = viewModel.viewEvent) { event ->
		when (event) {
			is Evaluations.Event.ShowSnackBar ->
				showSnackBar(event.message, null, null)

			is Evaluations.Event.ShowFilterEvaluationsDialog ->
				dialogState.value = EvaluationsDialog.Filter(filters = event.filters)

			is Evaluations.Event.CloseDialog ->
				dialogState.value = null
		}
	}

	LaunchedEffect(Unit) {
		viewModel.loadEvaluationsAction()
	}

	when (val state = dialogState.value) {
		is EvaluationsDialog.Filter ->
			FilterDialog(
				sheetState = sheetState,
				filters = state.filters,
				onApplyClick = {
					// TODO
				},
				onDismissRequest = viewModel::closeDialogAction
			)

		null -> {}
	}

	EvaluationsScreen(
		state = viewState,
		onRetryClick = viewModel::loadEvaluationsAction,
		onAddEvaluationClick = viewModel::addEvaluationAction,
	)
}