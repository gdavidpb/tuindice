package com.gdavidpb.tuindice.evaluations.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationViewModel
import com.gdavidpb.tuindice.evaluations.ui.screen.EvaluationScreen

@Composable
fun EvaluationRoute(
	evaluationId: String?,
	onNavigateToEvaluations: () -> Unit,
	onNavigateToGradePickerDialog: (evaluationName: String, subjectCode: String, grade: Double?, maxGrade: Double?) -> Unit,
	onNavigateToMaxGradePickerDialog: (evaluationName: String, subjectCode: String, maxGrade: Double?) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	viewModel: EvaluationViewModel
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is Evaluation.Effect.NavigateToEvaluations ->
				onNavigateToEvaluations()

			is Evaluation.Effect.NavigateToGradePickerDialog ->
				onNavigateToGradePickerDialog(
					effect.evaluationName,
					effect.subjectCode,
					effect.grade,
					effect.maxGrade
				)

			is Evaluation.Effect.NavigateToMaxGradePickerDialog ->
				onNavigateToMaxGradePickerDialog(
					effect.evaluationName,
					effect.subjectCode,
					effect.maxGrade
				)

			is Evaluation.Effect.ShowSnackBar ->
				showSnackBar(SnackBarMessage(message = effect.message))
		}
	}

	LaunchedEffect(Unit) {
		if (evaluationId == null)
			viewModel.loadAvailableAttemptsAction()
		else
			viewModel.loadEvaluationAction(evaluationId)
	}

	EvaluationScreen(
		state = viewState,
		onAttemptChange = viewModel::setAttemptAction,
		onTypeChange = viewModel::setTypeAction,
		onDateChange = viewModel::setDateAction,
		onGradeClick = viewModel::clickGradeAction,
		onMaxGradeClick = viewModel::clickMaxGradeAction,
		onDoneClick = { attempt, type, scheduleMode, date, grade, maxGrade ->
			if (evaluationId == null) {
				viewModel.clickAddEvaluationAction(
					attempt = attempt,
					type = type,
					scheduleMode = scheduleMode,
					date = date,
					grade = grade,
					maxGrade = maxGrade
				)
			} else {
				viewModel.clickEditEvaluationAction(
					evaluationId = evaluationId,
					attempt = attempt,
					type = type,
					scheduleMode = scheduleMode,
					date = date,
					grade = grade,
					maxGrade = maxGrade
				)
			}
		},
		onRetryClick = {
			if (evaluationId == null)
				viewModel.loadAvailableAttemptsAction()
			else
				viewModel.loadEvaluationAction(evaluationId)
		}
	)
}
