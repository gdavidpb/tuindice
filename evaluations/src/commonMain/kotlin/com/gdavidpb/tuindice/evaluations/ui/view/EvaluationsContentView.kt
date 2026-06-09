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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.InternalScreenDefaults
import com.gdavidpb.tuindice.base.ui.view.EmptyStateAnimationView
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.message_empty_match_evaluations
import tuindice.evaluations.generated.resources.title_empty_match_evaluations

@Composable
fun EvaluationsContentView(
	state: Evaluations.State.Content,
	onAddEvaluationClick: () -> Unit,
	onWeekClick: (EvaluationsWeekKey) -> Unit = {},
	onEvaluationClick: (evaluationId: String, evaluationName: String, subjectCode: String) -> Unit,
	onEvaluationEdit: (evaluationId: String) -> Unit,
	onEvaluationDelete: (evaluationId: String) -> Unit,
	scrollEnabled: Boolean = true,
	openActionsEvaluationId: String? = null,
	focusEvaluationId: String? = null,
	onFocusEvaluationBoundsChange: (Rect?) -> Unit = {}
) {
	val initialFirstVisibleItemIndex = remember(state.evaluationWeekGroups, state.selectedWeekKey) {
		state.evaluationWeekGroups.initialFirstVisibleItemIndex(state.selectedWeekKey)
	}
	val lazyColumState = rememberLazyListState(
		initialFirstVisibleItemIndex = initialFirstVisibleItemIndex
	)
	val hasEvaluationItems = state.evaluationWeekGroups.any { weekGroup ->
		weekGroup.groups.any { group -> group.items.isNotEmpty() }
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
			EvaluationsWeekStripView(
				items = state.weekItems,
				selectedWeekKey = state.selectedWeekKey,
				onWeekSelected = onWeekClick,
				modifier = Modifier.padding(top = 6.dp)
			)

			if (hasEvaluationItems) {
				EvaluationsView(
					lazyListState = lazyColumState,
					weekGroups = state.evaluationWeekGroups,
					selectedWeekKey = state.selectedWeekKey,
					onVisibleWeekChange = onWeekClick,
					onEvaluationClick = onEvaluationClick,
					onEvaluationEdit = onEvaluationEdit,
					onEvaluationDelete = onEvaluationDelete,
					scrollEnabled = scrollEnabled,
					openActionsEvaluationId = openActionsEvaluationId,
					focusEvaluationId = focusEvaluationId,
					onFocusEvaluationBoundsChange = onFocusEvaluationBoundsChange
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
			visible = !scrollEnabled || !lazyColumState.isScrollInProgress,
			enter = fadeIn(),
			exit = fadeOut()
		) {
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

private fun List<EvaluationsWeekGroupItem>.initialFirstVisibleItemIndex(
	selectedWeekKey: EvaluationsWeekKey
): Int {
	var index = 0

	forEach { weekGroup ->
		val evaluationCount = weekGroup.groups.sumOf { group -> group.items.size }

		if (evaluationCount > 0) {
			if (weekGroup.key == selectedWeekKey) {
				return index
			}

			index += 1 + evaluationCount
		}
	}

	return 0
}
