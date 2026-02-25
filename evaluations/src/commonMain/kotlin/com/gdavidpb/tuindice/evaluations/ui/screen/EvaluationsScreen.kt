package com.gdavidpb.tuindice.evaluations.ui.screen

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.ui.view.SealedCrossfade
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations

@Composable
fun EvaluationsScreen(
	state: Evaluations.State,
	onAddEvaluationClick: () -> Unit,
	onEvaluationClick: (evaluationId: String) -> Unit,
	onEvaluationEdit: (evaluationId: String) -> Unit,
	onEvaluationDelete: (evaluationId: String) -> Unit,
	onFilterCheckedChange: (filter: EvaluationFilter, isChecked: Boolean) -> Unit,
	onClearFiltersClick: () -> Unit,
	onRetryClick: () -> Unit,
	loadingContent: @Composable () -> Unit,
	contentStateContent: @Composable (
		state: Evaluations.State.Content,
		onAddEvaluationClick: () -> Unit,
		onEvaluationClick: (evaluationId: String) -> Unit,
		onEvaluationEdit: (evaluationId: String) -> Unit,
		onEvaluationDelete: (evaluationId: String) -> Unit,
		onFilterCheckedChange: (filter: EvaluationFilter, isChecked: Boolean) -> Unit,
		onClearFiltersClick: () -> Unit
	) -> Unit,
	failedContent: @Composable (onRetryClick: () -> Unit) -> Unit,
	noSubjectsContent: @Composable () -> Unit,
	emptyContent: @Composable (onAddEvaluationClick: () -> Unit) -> Unit
) {
	SealedCrossfade(
		targetState = state
	) { targetState ->
		when (targetState) {
			is Evaluations.State.Loading ->
				loadingContent()

			is Evaluations.State.Content ->
				contentStateContent(
					targetState,
					onAddEvaluationClick,
					onEvaluationClick,
					onEvaluationEdit,
					onEvaluationDelete,
					onFilterCheckedChange,
					onClearFiltersClick
				)

			is Evaluations.State.Failed ->
				failedContent(onRetryClick)

			is Evaluations.State.NoSubjects ->
				noSubjectsContent()

			is Evaluations.State.Empty ->
				emptyContent(onAddEvaluationClick)
		}
	}
}
