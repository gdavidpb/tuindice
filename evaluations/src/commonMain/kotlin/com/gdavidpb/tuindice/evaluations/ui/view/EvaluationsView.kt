package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.stringResource
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.evaluation_group_count_many
import tuindice.evaluations.generated.resources.evaluation_group_count_one

@Composable
fun EvaluationsView(
	lazyListState: LazyListState,
	weekGroups: List<EvaluationsWeekGroupItem>,
	selectedWeekKey: EvaluationsWeekKey,
	onVisibleWeekChange: (EvaluationsWeekKey) -> Unit,
	onEvaluationClick: (evaluationId: String, evaluationName: String, subjectCode: String) -> Unit,
	onEvaluationEdit: (evaluationId: String) -> Unit,
	onEvaluationDelete: (evaluationId: String) -> Unit,
	scrollEnabled: Boolean = true,
	openActionsEvaluationId: String? = null,
	focusEvaluationId: String? = null,
	onFocusEvaluationBoundsChange: (Rect?) -> Unit = {}
) {
	val singleCountPattern = stringResource(Res.string.evaluation_group_count_one)
	val manyCountPattern = stringResource(Res.string.evaluation_group_count_many)
	val weekHeaderIndexes = remember(weekGroups) {
		weekGroups.weekHeaderIndexes()
	}
	val isProgrammaticWeekScroll = remember { mutableStateOf(false) }
	val hasPositionedSelectedWeek = remember { mutableStateOf(false) }

	LaunchedEffect(selectedWeekKey, weekHeaderIndexes) {
		val targetIndex = weekHeaderIndexes[selectedWeekKey] ?: return@LaunchedEffect

		if (!lazyListState.isScrollInProgress && lazyListState.firstVisibleItemIndex != targetIndex) {
			isProgrammaticWeekScroll.value = true
			try {
				if (hasPositionedSelectedWeek.value) {
					lazyListState.animateScrollToItem(targetIndex)
				} else {
					lazyListState.scrollToItem(targetIndex)
					hasPositionedSelectedWeek.value = true
				}
			} finally {
				isProgrammaticWeekScroll.value = false
			}
		} else {
			hasPositionedSelectedWeek.value = true
		}
	}

	LaunchedEffect(lazyListState, weekHeaderIndexes, selectedWeekKey) {
		snapshotFlow { lazyListState.firstVisibleItemIndex }
			.map { index -> weekHeaderIndexes.visibleWeekKey(index) }
			.distinctUntilChanged()
			.collect { weekKey ->
				if (
					weekKey != null &&
					weekKey != selectedWeekKey &&
					lazyListState.isScrollInProgress &&
					!isProgrammaticWeekScroll.value
				) {
					onVisibleWeekChange(weekKey)
				}
			}
	}

	LazyColumn(
		modifier = Modifier.testTag(EvaluationsUiTags.EvaluationsList),
		state = lazyListState,
		userScrollEnabled = scrollEnabled,
		contentPadding = PaddingValues(bottom = EvaluationsListBottomPadding)
	) {
		weekGroups.forEach { weekGroup ->
			val evaluations = weekGroup.evaluationItems()

			if (evaluations.isEmpty()) {
				return@forEach
			}

			stickyHeader(
				key = "week_header:${weekGroup.key.tagSuffix}"
			) {
				EvaluationWeekHeaderView(
					weekKey = weekGroup.key,
					label = weekGroup.title,
					countText = evaluations.countText(
						singleCountPattern = singleCountPattern,
						manyCountPattern = manyCountPattern
					)
				)
			}

			items(
				items = evaluations,
				key = { evaluation -> evaluation.evaluationId },
				contentType = { EvaluationItemContentType }
			) { evaluation ->
				val itemModifier = if (evaluation.evaluationId == focusEvaluationId) {
					Modifier.onGloballyPositioned { coordinates ->
						onFocusEvaluationBoundsChange(coordinates.boundsInRoot())
					}
				} else {
					Modifier
				}

				EvaluationSwipeToDismiss(
					modifier = itemModifier.animateItem(
						fadeInSpec = null,
						fadeOutSpec = null
					),
					initiallyOpen = evaluation.evaluationId == openActionsEvaluationId,
					onEdit = { onEvaluationEdit(evaluation.evaluationId) },
					onDelete = { onEvaluationDelete(evaluation.evaluationId) }
				) { onActionsClick ->
					EvaluationItemView(
						item = evaluation,
						onGradeClick = {
							if (evaluation.isClickable) {
								onEvaluationClick(
									evaluation.evaluationId,
									evaluation.nameText,
									evaluation.subjectCodeText
								)
							}
						},
						onCardClick = onActionsClick
					)
				}
			}
		}
	}
}

private fun List<EvaluationsWeekGroupItem>.weekHeaderIndexes(): Map<EvaluationsWeekKey, Int> {
	var index = 0
	val indexes = mutableMapOf<EvaluationsWeekKey, Int>()

	forEach { weekGroup ->
		val evaluationCount = weekGroup.evaluationItems().size

		if (evaluationCount > 0) {
			indexes[weekGroup.key] = index
			index += 1
			index += evaluationCount
		}
	}

	return indexes
}

private fun Map<EvaluationsWeekKey, Int>.visibleWeekKey(index: Int): EvaluationsWeekKey? {
	return entries
		.sortedBy { (_, itemIndex) -> itemIndex }
		.lastOrNull { (_, itemIndex) -> itemIndex <= index }
		?.key
}

private fun EvaluationsWeekGroupItem.evaluationItems(): List<EvaluationItem> =
	groups.flatMap { group -> group.items }

private fun List<EvaluationItem>.countText(
	singleCountPattern: String,
	manyCountPattern: String
): String {
	val pattern = if (size == 1) singleCountPattern else manyCountPattern

	return pattern.replace("%1${'$'}d", size.toString())
}

private val EvaluationsListBottomPadding = 120.dp
private const val EvaluationItemContentType = "evaluation_item"
