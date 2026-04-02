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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.InternalScreenDefaults
import com.gdavidpb.tuindice.base.ui.view.EmptyStateAnimationView
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.mapper.rememberEvaluationItemMapping
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toEvaluationFilterGroupItemList
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toEvaluationItemList
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.message_empty_match_evaluations
import tuindice.evaluations.generated.resources.title_empty_match_evaluations

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
	val lazyColumState = rememberLazyListState()
	val evaluations = remember(state.filteredEvaluations, mapping) {
		state
			.filteredEvaluations
			.toEvaluationItemList(mapping = mapping)
	}
	val filterGroups = remember(
		state.availableFilters,
		state.activeFilters
	) {
		state.availableFilters.toEvaluationFilterGroupItemList(
			activeFilters = state.activeFilters
		)
	}

	Box(
		modifier = Modifier
			.testTag(EvaluationsUiTags.EvaluationsContentContainer)
			.fillMaxSize()
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(top = InternalScreenDefaults.TopBarSpacing)
			) {
				EvaluationFilterView(
					groups = filterGroups,
					onFilterCheckedChange = onFilterCheckedChange
				)

			if (evaluations.isNotEmpty()) {
				EvaluationsView(
					lazyListState = lazyColumState,
					evaluations = evaluations,
					onEvaluationClick = onEvaluationClick,
					onEvaluationEdit = onEvaluationEdit,
					onEvaluationDelete = onEvaluationDelete
				)
			} else {
				EvaluationsEmptyMatchView(
					title = stringResource(Res.string.title_empty_match_evaluations),
					message = stringResource(Res.string.message_empty_match_evaluations),
					headerContent = {
						EmptyStateAnimationView()
					}
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
							.testTag(EvaluationsUiTags.EvaluationsClearFiltersFab)
							.padding(bottom = 16.dp),
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
					modifier = Modifier.testTag(EvaluationsUiTags.EvaluationsAddFab),
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
