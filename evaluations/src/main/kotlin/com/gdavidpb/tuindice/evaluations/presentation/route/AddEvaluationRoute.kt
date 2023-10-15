package com.gdavidpb.tuindice.evaluations.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.evaluations.presentation.contract.AddEvaluation
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.AddEvaluationViewModel
import com.gdavidpb.tuindice.evaluations.ui.screen.AddEvaluationScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddEvaluationRoute(
	showSnackBar: (message: String, actionLabel: String?, action: (() -> Unit)?) -> Unit,
	viewModel: AddEvaluationViewModel = koinViewModel()
) {
	val viewState by viewModel.viewState.collectAsStateWithLifecycle()

	CollectEffectWithLifecycle(flow = viewModel.viewEvent) { event ->
		when (event) {
			is AddEvaluation.Event.ShowSnackBar ->
				showSnackBar(event.message, null, null)
		}
	}

	LaunchedEffect(Unit) {
		viewModel.loadAvailableSubjectsAction()
	}

	AddEvaluationScreen(
		state = viewState,
		onSubjectChange = viewModel::setSubject,
		onTypeChange = viewModel::setType,
		onDateChange = viewModel::setDate,
		onMaxGradeChange = viewModel::setMaxGrade,
		onDoneClick = viewModel::clickDoneAction,
		onRetryClick = viewModel::loadAvailableSubjectsAction
	)
}