package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FilterAltOff
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.view.EmptyStateAnimationView
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.mapper.rememberEvaluationItemMapping
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toEvaluationItemList
import org.jetbrains.compose.resources.stringResource
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.message_empty_match_evaluations
import tuindice.evaluations.generated.resources.title_empty_match_evaluations

@Composable
fun EvaluationsContentView(
	state: Evaluations.State.Content,
	hasEvaluations: Boolean,
	emptyMatchTitle: String,
	emptyMatchMessage: String,
	onAddEvaluationClick: () -> Unit,
	onClearFiltersClick: () -> Unit,
	onFilterCheckedChange: (filter: EvaluationFilter, isChecked: Boolean) -> Unit,
	onEvaluationClick: (evaluationId: String) -> Unit,
	onEvaluationEdit: (evaluationId: String) -> Unit,
	onEvaluationDelete: (evaluationId: String) -> Unit,
	addFabContent: @Composable () -> Unit,
	clearFiltersFabContent: @Composable () -> Unit,
	emptyMatchHeaderContent: @Composable () -> Unit,
	evaluationsContent: @Composable (
		lazyListState: LazyListState
	) -> Unit
) {
	val lazyColumState = rememberLazyListState()

	Box(
		modifier = Modifier
			.fillMaxSize()
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
		) {
			EvaluationFilterView(
				availableFilters = state.availableFilters,
				activeFilters = state.activeFilters,
				onFilterCheckedChange = onFilterCheckedChange
			)

			if (hasEvaluations) {
				evaluationsContent(
					lazyColumState
				)
			} else {
				EvaluationsEmptyMatchView(
					title = emptyMatchTitle,
					message = emptyMatchMessage,
					headerContent = emptyMatchHeaderContent
				)
			}
		}

		AnimatedVisibility(
			modifier = Modifier
				.align(Alignment.BottomEnd)
				.padding(24.dp),
			visible = !lazyColumState.isScrollInProgress,
			enter = fadeIn(),
			exit = fadeOut()
		) {
			Column(
				horizontalAlignment = Alignment.End
			) {
				if (state.activeFilters.isNotEmpty())
					SmallFloatingActionButton(
						modifier = Modifier
							.padding(bottom = 16.dp),
						containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
						contentColor = MaterialTheme.colorScheme.primaryContainer,
						onClick = onClearFiltersClick
					) {
						clearFiltersFabContent()
					}

				FloatingActionButton(
					containerColor = MaterialTheme.colorScheme.primary,
					onClick = onAddEvaluationClick
				) {
					addFabContent()
				}
			}
		}
	}
}

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
	val mapping = rememberEvaluationItemMapping()
	val evaluations = state
		.filteredEvaluations
		.toEvaluationItemList(mapping = mapping)

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
