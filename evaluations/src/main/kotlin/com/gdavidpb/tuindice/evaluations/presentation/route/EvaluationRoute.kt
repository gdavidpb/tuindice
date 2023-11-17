package com.gdavidpb.tuindice.evaluations.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.model.rememberDialogState
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationDialog
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationViewModel
import com.gdavidpb.tuindice.evaluations.ui.dialog.GradePickerDialog
import com.gdavidpb.tuindice.evaluations.ui.screen.EvaluationScreen
import com.gdavidpb.tuindice.evaluations.ui.view.custom.grade.EvaluationGradeWheelPickerDefaults
import com.gdavidpb.tuindice.evaluations.ui.view.custom.grade.utils.MAX_EVALUATION_GRADE
import com.gdavidpb.tuindice.evaluations.ui.view.custom.grade.utils.MIN_EVALUATION_GRADE
import org.koin.androidx.compose.koinViewModel

@Composable
fun EvaluationRoute(
	evaluationId: String?,
	onNavigateToEvaluations: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	viewModel: EvaluationViewModel = koinViewModel()
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()
	val dialogState = rememberDialogState<EvaluationDialog>()

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is Evaluation.Effect.NavigateToEvaluations ->
				onNavigateToEvaluations()

			is Evaluation.Effect.ShowSnackBar ->
				showSnackBar(SnackBarMessage(message = effect.message))

			is Evaluation.Effect.ShowGradePickerDialog ->
				dialogState.value = EvaluationDialog.GradePicker(
					grade = effect.grade,
					maxGrade = effect.maxGrade
				)

			is Evaluation.Effect.ShowMaxGradePickerDialog ->
				dialogState.value = EvaluationDialog.MaxGradePicker(
					grade = effect.maxGrade
				)

			is Evaluation.Effect.CloseDialog ->
				dialogState.value = null
		}
	}

	LaunchedEffect(Unit) {
		if (evaluationId == null)
			viewModel.loadAvailableSubjectsAction()
		else
			viewModel.loadEvaluationAction(evaluationId)
	}

	when (val dialog = dialogState.value) {
		is EvaluationDialog.GradePicker ->
			GradePickerDialog(
				title = stringResource(id = R.string.dialog_title_add_evaluation_grade),
				selectedGrade = dialog.grade,
				gradeRange = MIN_EVALUATION_GRADE..(dialog.maxGrade ?: MAX_EVALUATION_GRADE),
				onGradeChange = viewModel::setGradeAction,
				onDismissRequest = viewModel::closeDialogAction
			)

		is EvaluationDialog.MaxGradePicker ->
			GradePickerDialog(
				title = stringResource(id = R.string.dialog_title_add_evaluation_max_grade),
				selectedGrade = dialog.grade,
				gradeRange = EvaluationGradeWheelPickerDefaults.GradeRange,
				onGradeChange = viewModel::setMaxGradeAction,
				onDismissRequest = viewModel::closeDialogAction
			)

		else -> {}
	}

	EvaluationScreen(
		state = viewState,
		onSubjectChange = viewModel::setSubjectAction,
		onTypeChange = viewModel::setTypeAction,
		onDateChange = viewModel::setDateAction,
		onGradeClick = viewModel::clickGradeAction,
		onMaxGradeClick = viewModel::clickMaxGradeAction,
		onDoneClick = { subject, type, date, grade, maxGrade ->
			if (evaluationId == null)
				viewModel.clickAddEvaluationAction(
					subject = subject,
					type = type,
					date = date,
					grade = grade,
					maxGrade = maxGrade
				)
			else
				viewModel.clickEditEvaluationAction(
					evaluationId = evaluationId,
					subject = subject,
					type = type,
					date = date,
					grade = grade,
					maxGrade = maxGrade
				)
		},
		onRetryClick = {
			if (evaluationId == null)
				viewModel.loadAvailableSubjectsAction()
			else
				viewModel.loadEvaluationAction(evaluationId)
		}
	)
}