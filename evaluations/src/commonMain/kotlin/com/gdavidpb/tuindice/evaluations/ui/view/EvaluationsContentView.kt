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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations

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
