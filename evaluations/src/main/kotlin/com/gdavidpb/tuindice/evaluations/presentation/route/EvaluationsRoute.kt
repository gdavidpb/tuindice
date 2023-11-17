package com.gdavidpb.tuindice.evaluations.presentation.route

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
import com.gdavidpb.tuindice.evaluations.ui.dialog.GradePickerDialog
import com.gdavidpb.tuindice.evaluations.ui.screen.EvaluationsScreen
import com.gdavidpb.tuindice.evaluations.ui.view.custom.grade.utils.MIN_EVALUATION_GRADE
import org.koin.androidx.compose.koinViewModel

@Composable
fun EvaluationsRoute(
	onNavigateToAddEvaluation: () -> Unit,
	onNavigateToEvaluation: (evaluationId: String) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	viewModel: EvaluationsViewModel = koinViewModel()
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()
	val dialogState = rememberDialogState<EvaluationsDialog>()

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is Evaluations.Effect.NavigateToAddEvaluation ->
				onNavigateToAddEvaluation()

			is Evaluations.Effect.NavigateToEvaluation ->
				onNavigateToEvaluation(
					effect.evaluationId
				)

			is Evaluations.Effect.ShowGradePickerDialog ->
				dialogState.value = EvaluationsDialog.GradePicker(
					evaluationId = effect.evaluationId,
					grade = effect.grade,
					maxGrade = effect.maxGrade
				)

			is Evaluations.Effect.ShowSnackBar ->
				showSnackBar(
					SnackBarMessage(
						message = effect.message
					)
				)

			is Evaluations.Effect.CloseDialog ->
				dialogState.value = null
		}
	}

	LaunchedEffect(Unit) {
		viewModel.loadEvaluationsAction()
	}

	when (val dialog = dialogState.value) {
		is EvaluationsDialog.GradePicker ->
			GradePickerDialog(
				selectedGrade = dialog.grade,
				gradeRange = MIN_EVALUATION_GRADE..dialog.maxGrade,
				onGradeChange = { grade ->
					viewModel.setEvaluationGradeAction(
						evaluationId = dialog.evaluationId,
						grade = grade
					)
				},
				onDismissRequest = viewModel::closeDialogAction
			)

		else -> {}
	}

	EvaluationsScreen(
		state = viewState,
		onAddEvaluationClick = viewModel::addEvaluationAction,
		onEvaluationClick = viewModel::showEvaluationGradeDialogAction,
		onEvaluationEdit = viewModel::editEvaluationAction,
		onEvaluationDelete = viewModel::removeEvaluationAction,
		onClearFiltersClick = viewModel::clearFiltersAction,
		onFilterCheckedChange = viewModel::toggleFilterAction,
		onRetryClick = viewModel::loadEvaluationsAction
	)
}