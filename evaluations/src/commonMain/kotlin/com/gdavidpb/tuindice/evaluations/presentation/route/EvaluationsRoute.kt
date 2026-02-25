package com.gdavidpb.tuindice.evaluations.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationsViewModel

@Composable
fun EvaluationsRoute(
	onNavigateToAddEvaluation: () -> Unit,
	onNavigateToEvaluation: (evaluationId: String) -> Unit,
	onNavigateToEvaluationGradePickerDialog: (evaluationId: String, grade: Double, maxGrade: Double) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	viewModel: EvaluationsViewModel,
	content: @Composable (
		state: Evaluations.State,
		onAddEvaluationClick: () -> Unit,
		onEvaluationClick: (evaluationId: String) -> Unit,
		onEvaluationEdit: (evaluationId: String) -> Unit,
		onEvaluationDelete: (evaluationId: String) -> Unit,
		onFilterCheckedChange: (filter: EvaluationFilter, isChecked: Boolean) -> Unit,
		onClearFiltersClick: () -> Unit,
		onRetryClick: () -> Unit
	) -> Unit
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is Evaluations.Effect.NavigateToAddEvaluation ->
				onNavigateToAddEvaluation()

			is Evaluations.Effect.NavigateToEvaluation ->
				onNavigateToEvaluation(
					effect.evaluationId
				)

			is Evaluations.Effect.NavigateToGradePickerDialog ->
				onNavigateToEvaluationGradePickerDialog(
					effect.evaluationId,
					effect.grade,
					effect.maxGrade
				)

			is Evaluations.Effect.ShowSnackBar ->
				showSnackBar(
					SnackBarMessage(
						message = effect.message
					)
				)
		}
	}

	LaunchedEffect(Unit) {
		viewModel.loadEvaluationsAction()
	}

	content(
		viewState,
		viewModel::addEvaluationAction,
		viewModel::showEvaluationGradeDialogAction,
		viewModel::editEvaluationAction,
		viewModel::removeEvaluationAction,
		viewModel::toggleFilterAction,
		viewModel::clearFiltersAction,
		viewModel::loadEvaluationsAction
	)
}
