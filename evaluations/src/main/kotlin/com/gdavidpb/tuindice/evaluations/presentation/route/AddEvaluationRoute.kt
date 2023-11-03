package com.gdavidpb.tuindice.evaluations.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.model.rememberDialogState
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.evaluations.presentation.contract.AddEvaluation
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationDialog
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.AddEvaluationViewModel
import com.gdavidpb.tuindice.evaluations.ui.dialog.GradePickerDialog
import com.gdavidpb.tuindice.evaluations.ui.screen.AddEvaluationScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddEvaluationRoute(
	onNavigateToEvaluations: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	viewModel: AddEvaluationViewModel = koinViewModel()
) {
	val viewState by viewModel.viewState.collectAsStateWithLifecycle()
	val dialogState = rememberDialogState<EvaluationDialog>()

	CollectEffectWithLifecycle(flow = viewModel.viewEvent) { event ->
		when (event) {
			is AddEvaluation.Event.NavigateToEvaluations ->
				onNavigateToEvaluations()

			is AddEvaluation.Event.ShowSnackBar ->
				showSnackBar(SnackBarMessage(message = event.message))

			is AddEvaluation.Event.ShowGradePickerDialog ->
				dialogState.value = EvaluationDialog.GradePicker(
					grade = event.grade
				)

			is AddEvaluation.Event.ShowMaxGradePickerDialog ->
				dialogState.value = EvaluationDialog.MaxGradePicker(
					grade = event.grade
				)

			is AddEvaluation.Event.CloseDialog ->
				dialogState.value = null
		}
	}

	LaunchedEffect(Unit) {
		viewModel.loadAvailableSubjectsAction()
	}

	when (val dialog = dialogState.value) {
		is EvaluationDialog.GradePicker ->
			GradePickerDialog(
				selectedGrade = dialog.grade,
				onGradeChange = viewModel::setGrade,
				onDismissRequest = viewModel::closeDialogAction
			)

		is EvaluationDialog.MaxGradePicker ->
			GradePickerDialog(
				selectedGrade = dialog.grade,
				onGradeChange = viewModel::setMaxGrade,
				onDismissRequest = viewModel::closeDialogAction
			)

		else -> {}
	}

	AddEvaluationScreen(
		state = viewState,
		onSubjectChange = viewModel::setSubject,
		onTypeChange = viewModel::setType,
		onDateChange = viewModel::setDate,
		onGradeClick = viewModel::clickGradeAction,
		onMaxGradeClick = viewModel::clickMaxGradeAction,
		onDoneClick = viewModel::clickDoneAction,
		onRetryClick = viewModel::loadAvailableSubjectsAction
	)
}