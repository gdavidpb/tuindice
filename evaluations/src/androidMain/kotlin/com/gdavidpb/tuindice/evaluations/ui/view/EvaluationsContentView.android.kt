package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FilterAltOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.ui.view.EmptyStateAnimationView
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toEvaluationItemList
import org.jetbrains.compose.resources.stringResource
import tuindice.evaluations.generated.resources.*

@Composable
fun EvaluationsContentView(
	state: Evaluations.State.Content,
	onAddEvaluationClick: () -> Unit,
	onClearFiltersClick: () -> Unit,
	onFilterCheckedChange: (filter: EvaluationFilter, isChecked: Boolean) -> Unit,
	onEvaluationClick: (evaluationId: String) -> Unit,
	onEvaluationEdit: (evaluationId: String) -> Unit,
	onEvaluationDelete: (evaluationId: String) -> Unit
) {
	val evaluations = state
		.filteredEvaluations
		.toEvaluationItemList()

	EvaluationsContentView(
		state = state,
		hasEvaluations = evaluations.isNotEmpty(),
		emptyMatchTitle = stringResource(Res.string.title_empty_match_evaluations),
		emptyMatchMessage = stringResource(Res.string.message_empty_match_evaluations),
		onAddEvaluationClick = onAddEvaluationClick,
		onClearFiltersClick = onClearFiltersClick,
		onFilterCheckedChange = onFilterCheckedChange,
		onEvaluationClick = onEvaluationClick,
		onEvaluationEdit = onEvaluationEdit,
		onEvaluationDelete = onEvaluationDelete,
		addFabContent = {
			Icon(
				imageVector = Icons.Outlined.Add,
				contentDescription = null
			)
		},
		clearFiltersFabContent = {
			Icon(
				imageVector = Icons.Outlined.FilterAltOff,
				contentDescription = null
			)
		},
		emptyMatchHeaderContent = {
			EmptyStateAnimationView()
		},
		evaluationsContent = { lazyListState ->
			EvaluationsView(
				lazyListState = lazyListState,
				evaluations = evaluations,
				onEvaluationClick = onEvaluationClick,
				onEvaluationEdit = onEvaluationEdit,
				onEvaluationDelete = onEvaluationDelete
			)
		}
	)
}
