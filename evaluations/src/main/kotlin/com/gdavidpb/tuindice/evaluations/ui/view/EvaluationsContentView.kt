package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.dimensionResource
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.mapper.formatAsToNow
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toEvaluationGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toEvaluationItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsGroupItem

@Composable
fun EvaluationsContentView(
	state: Evaluations.State.Content,
	onAddEvaluationClick: () -> Unit,
	onClearFiltersClick: () -> Unit,
	onFilterCheckedChange: (filter: EvaluationFilter, isChecked: Boolean) -> Unit,
	onEvaluationClick: (evaluation: EvaluationItem) -> Unit,
	onEvaluationDelete: (evaluation: EvaluationItem) -> Unit,
	onEvaluationIsCompletedChange: (evaluation: EvaluationItem, isCompleted: Boolean) -> Unit
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

			if (state.filteredEvaluations.isNotEmpty()) {
				val evaluations = state
					.filteredEvaluations
					.toEvaluationGroupItem()

				EvaluationsView(
					evaluations = evaluations,
					lazyListState = lazyColumState,
					onEvaluationClick = onEvaluationClick,
					onEvaluationDelete = onEvaluationDelete,
					onEvaluationIsCompletedChange = onEvaluationIsCompletedChange
				)
			} else
				EvaluationsEmptyMatchView()
		}

		AnimatedVisibility(
			modifier = Modifier
				.align(Alignment.BottomEnd)
				.padding(dimensionResource(id = R.dimen.dp_24)),
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
							.padding(
								bottom = dimensionResource(id = R.dimen.dp_16)
							),
						containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
						contentColor = MaterialTheme.colorScheme.primaryContainer,
						onClick = onClearFiltersClick
					) {
						Icon(
							imageVector = Icons.Outlined.FilterAltOff,
							contentDescription = null
						)
					}

				FloatingActionButton(
					containerColor = MaterialTheme.colorScheme.primary,
					onClick = onAddEvaluationClick
				) {
					Icon(
						imageVector = Icons.Outlined.Add,
						contentDescription = null
					)
				}
			}
		}
	}
}