package com.gdavidpb.tuindice.evaluations.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.mapper.EvaluationItemMappingProvider
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationsViewModel
import com.gdavidpb.tuindice.evaluations.ui.screen.EvaluationsScreen

@Composable
fun EvaluationsRoute(
	onNavigateToAddEvaluation: () -> Unit,
	onNavigateToEvaluation: (evaluationId: String) -> Unit,
	onNavigateToEvaluationGradePickerDialog: (evaluationId: String, grade: Double, maxGrade: Double) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	mappingProvider: EvaluationItemMappingProvider,
	viewModel: EvaluationsViewModel
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

	EvaluationsScreen(
		state = viewState,
		mappingProvider = mappingProvider,
		onAddEvaluationClick = viewModel::addEvaluationAction,
		onEvaluationClick = viewModel::showEvaluationGradeDialogAction,
		onEvaluationEdit = viewModel::editEvaluationAction,
		onEvaluationDelete = viewModel::removeEvaluationAction,
		onFilterCheckedChange = viewModel::toggleFilterAction,
		onClearFiltersClick = viewModel::clearFiltersAction,
		onRetryClick = viewModel::loadEvaluationsAction
	)
}
