package com.gdavidpb.tuindice.evaluations.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.evaluations.presentation.contract.AddEvaluation
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.AddEvaluationViewModel
import com.gdavidpb.tuindice.evaluations.ui.screen.AddEvaluationScreen

@Composable
fun AddEvaluationRoute(
	subRoute: Destination.AddEvaluation,
	onNavigateTo: (subRoute: Destination.AddEvaluation) -> Unit,
	showSnackBar: (message: String, actionLabel: String?, action: (() -> Unit)?) -> Unit,
	viewModel: AddEvaluationViewModel
) {
	val viewState by viewModel.viewState.collectAsStateWithLifecycle()

	CollectEffectWithLifecycle(flow = viewModel.viewEvent) { event ->
		when (event) {
			is AddEvaluation.Event.NavigateTo ->
				onNavigateTo(event.subRoute)

			is AddEvaluation.Event.ShowSnackBar ->
				showSnackBar(event.message, null, null)
		}
	}

	LaunchedEffect(Unit) {
		when (subRoute) {
			Destination.AddEvaluation.Step1 ->
				viewModel.loadStep1Action()

			Destination.AddEvaluation.Step2 -> {
				// TODO
			}
		}
	}

	AddEvaluationScreen(
		state = viewState,
		onSubjectChange = viewModel::setSubject,
		onTypeChange = viewModel::setType,
		onDateChange = viewModel::setDate,
		onMaxGradeChange = viewModel::setMaxGrade,
		onNextStepClick = viewModel::goNextStepAction,
		onRetryClick = viewModel::loadStep1Action
	)
}