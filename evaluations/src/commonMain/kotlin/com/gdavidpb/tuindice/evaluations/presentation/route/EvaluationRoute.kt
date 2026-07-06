package com.gdavidpb.tuindice.evaluations.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationViewModel
import com.gdavidpb.tuindice.evaluations.ui.dialog.DiscardEvaluationContentDialog
import com.gdavidpb.tuindice.evaluations.ui.screen.EvaluationScreen

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EvaluationRoute(
	evaluationId: String?,
	onNavigateToEvaluations: () -> Unit,
	onNavigateToGradePickerDialog: (evaluationName: String, subjectCode: String, grade: Double?, maxGrade: Double?) -> Unit,
	onNavigateToMaxGradePickerDialog: (evaluationName: String, subjectCode: String, maxGrade: Double?) -> Unit,
	onBack: () -> Unit = {},
	onBackInterceptorAvailable: ((() -> Boolean)?) -> Unit = {},
	showSnackBar: (message: SnackBarMessage) -> Unit,
	viewModel: EvaluationViewModel
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()
	val hasDiscardableInput = (viewState as? Evaluation.State.Content)?.hasDiscardableInput == true
	val hasDiscardableInputState = rememberUpdatedState(hasDiscardableInput)
	val showDiscardDialog = remember { mutableStateOf(false) }

	DisposableEffect(viewModel) {
		onBackInterceptorAvailable {
			if (hasDiscardableInputState.value) {
				showDiscardDialog.value = true
				true
			} else {
				false
			}
		}

		onDispose {
			onBackInterceptorAvailable(null)
		}
	}

	BackHandler(enabled = hasDiscardableInput) {
		showDiscardDialog.value = true
	}

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
		onDoneClick = viewModel::submitEvaluationAction,
		onRetryClick = {
			if (evaluationId == null)
				viewModel.loadAvailableAttemptsAction()
			else
				viewModel.loadEvaluationAction(evaluationId)
		}
	)

	if (showDiscardDialog.value) {
		DiscardEvaluationContentDialog(
			onConfirmClick = {
				showDiscardDialog.value = false
				onBack()
			},
			onDismissRequest = { showDiscardDialog.value = false }
		)
	}
}
