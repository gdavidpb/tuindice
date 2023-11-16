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
import com.gdavidpb.tuindice.evaluations.ui.view.custom.grade.EvaluationGradeWheelPickerDefaults
import com.gdavidpb.tuindice.evaluations.ui.view.custom.grade.utils.MIN_EVALUATION_GRADE
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddEvaluationRoute(
	onNavigateToEvaluations: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	viewModel: AddEvaluationViewModel = koinViewModel()
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()
	val dialogState = rememberDialogState<EvaluationDialog>()

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is AddEvaluation.Effect.NavigateToEvaluations ->
				onNavigateToEvaluations()

			is AddEvaluation.Effect.ShowSnackBar ->
				showSnackBar(SnackBarMessage(message = effect.message))

			is AddEvaluation.Effect.ShowGradePickerDialog ->
				dialogState.value = EvaluationDialog.GradePicker(
					grade = effect.grade,
					maxGrade = effect.maxGrade
				)

			is AddEvaluation.Effect.ShowMaxGradePickerDialog ->
				dialogState.value = EvaluationDialog.MaxGradePicker(
					grade = effect.grade
				)

			is AddEvaluation.Effect.CloseDialog ->
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
				gradeRange = MIN_EVALUATION_GRADE..dialog.maxGrade,
				onGradeChange = viewModel::setGradeAction,
				onDismissRequest = viewModel::closeDialogAction
			)

		is EvaluationDialog.MaxGradePicker ->
			GradePickerDialog(
				selectedGrade = dialog.grade,
				gradeRange = EvaluationGradeWheelPickerDefaults.GradeRange,
				onGradeChange = viewModel::setMaxGradeAction,
				onDismissRequest = viewModel::closeDialogAction
			)

		else -> {}
	}

	AddEvaluationScreen(
		state = viewState,
		onSubjectChange = viewModel::setSubjectAction,
		onTypeChange = viewModel::setTypeAction,
		onDateChange = viewModel::setDateAction,
		onGradeClick = viewModel::clickGradeAction,
		onMaxGradeClick = viewModel::clickMaxGradeAction,
		onDoneClick = viewModel::clickDoneAction,
		onRetryClick = viewModel::loadAvailableSubjectsAction
	)
}