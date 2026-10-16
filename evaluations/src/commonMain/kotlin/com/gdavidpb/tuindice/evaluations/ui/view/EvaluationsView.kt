package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.view.arrivalHighlight
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull
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
	onFocusEvaluationBoundsChange: (Rect?) -> Unit = {},
	arrivalEvaluationId: String? = null,
	onArrivalTaken: (evaluationId: String) -> Unit = {}
) {
	val singleCountPattern = stringResource(Res.string.evaluation_group_count_one)
	val manyCountPattern = stringResource(Res.string.evaluation_group_count_many)
	val weekHeaderIndexes = remember(weekGroups) {
		weekGroups.weekHeaderIndexes()
	}
	val lastWeekIndexes = remember(weekGroups) {
		weekGroups.lastWeekIndexes()
	}
	val isProgrammaticWeekScroll = remember { mutableStateOf(false) }
	val hasPositionedSelectedWeek = remember { mutableStateOf(false) }

	// The arrival is taken from the content once, here: the content forgets it and the list keeps it
	// until the highlight has played, so a later visit cannot replay it.
	val arrival = remember { mutableStateOf<EvaluationArrival?>(null) }
	val positionedArrival = remember { ArrivalCursor() }
	val highlightedArrival = remember { mutableStateOf<EvaluationArrival?>(null) }
	val positioning = remember(lazyListState) {
		WeekPositioning(
			lazyListState = lazyListState,
			isProgrammaticWeekScroll = isProgrammaticWeekScroll,
			hasPositionedSelectedWeek = hasPositionedSelectedWeek,
			positionedArrival = positionedArrival,
			highlightedArrival = highlightedArrival
		)
	}
	val lastWeekHeight = remember(weekGroups) { mutableStateOf<Int?>(null) }
	// Read apart from the layout info so the list end is not composed again on every scroll.
	val viewportHeight by remember(lazyListState) {
		derivedStateOf { lazyListState.layoutInfo.viewportSize.height }
	}

	LaunchedEffect(arrivalEvaluationId) {
		val evaluationId = arrivalEvaluationId ?: return@LaunchedEffect

		arrival.value = EvaluationArrival(evaluationId = evaluationId)
		onArrivalTaken(evaluationId)
	}

	LaunchedEffect(selectedWeekKey, weekHeaderIndexes, arrival.value) {
		val headerIndex = weekHeaderIndexes[selectedWeekKey] ?: return@LaunchedEffect

		positioning.positionWeek(
			weekGroups = weekGroups,
			weekKey = selectedWeekKey,
			headerIndex = headerIndex,
			take = arrival.value
		)
	}

	// The last week must be able to reach the top, so the list ends with what the viewport leaves below
	// it. Its height is kept once measured, and until then the whole viewport is left.
	LaunchedEffect(weekGroups, lastWeekIndexes) {
		val lastWeek = lastWeekIndexes ?: return@LaunchedEffect

		snapshotFlow { lazyListState.laidOutHeightOf(lastWeek) }
			.collect { height ->
				if (height != null) {
					lastWeekHeight.value = height
				}
			}
	}

	LaunchedEffect(lazyListState, weekHeaderIndexes, selectedWeekKey) {
		reportUserWeek(
			lazyListState = lazyListState,
			weekHeaderIndexes = weekHeaderIndexes,
			selectedWeekKey = selectedWeekKey,
			isProgrammaticWeekScroll = isProgrammaticWeekScroll,
			onVisibleWeekChange = onVisibleWeekChange
		)
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
				val highlight = highlightedArrival.value?.takeIf { highlighted ->
					highlighted.evaluationId == evaluation.evaluationId
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
						modifier = Modifier.arrivalHighlight(
							trigger = highlight,
							shape = MaterialTheme.shapes.medium,
							onFinished = {
								if (highlightedArrival.value === highlight) {
									highlightedArrival.value = null
								}
							}
						),
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
						onCardClick = onActionsClick,
						onEditAction = { onEvaluationEdit(evaluation.evaluationId) },
						onDeleteAction = { onEvaluationDelete(evaluation.evaluationId) }
					)
				}
			}
		}

		if (lastWeekIndexes != null) {
			item(key = LAST_WEEK_TAIL_KEY) {
				val density = LocalDensity.current

				Spacer(
					modifier = Modifier
						.testTag(EvaluationsUiTags.EvaluationsListTail)
						// What the viewport leaves below the last week once it is measured, never negative. Until then the
						// whole viewport, taken from the list itself so it is there from the first layout: the viewport the
						// list state reports is empty until then, and the first scroll to the last week would stop short.
						.then(
							lastWeekHeight.value?.let { measured ->
								Modifier.height(with(density) { (viewportHeight - measured).coerceAtLeast(0).toDp() })
							} ?: Modifier.fillParentMaxHeight()
						)
				)
			}
		}
	}
}

// One arrival taken from the content. Each take is a new object, so a jump is told apart from the one
// before it even when both name the same evaluation.
private class EvaluationArrival(
	val evaluationId: String
)

// The arrival the list has already positioned for. A selection change after it is not a new arrival.
private class ArrivalCursor {
	var value: EvaluationArrival? = null
}

// The list state the week positioning reads and writes, held together so the positioning can run outside
// the composable that owns it.
private class WeekPositioning(
	val lazyListState: LazyListState,
	val isProgrammaticWeekScroll: MutableState<Boolean>,
	val hasPositionedSelectedWeek: MutableState<Boolean>,
	val positionedArrival: ArrivalCursor,
	val highlightedArrival: MutableState<EvaluationArrival?>
) {
	// Positions the list on the week shown and, when an arrival is pending in it, on its evaluation. An arrival
	// is pending until the list has been positioned for it; it is marked positioned only once that is done, so
	// a restart in the middle of the scroll positions it again.
	suspend fun positionWeek(
		weekGroups: List<EvaluationsWeekGroupItem>,
		weekKey: EvaluationsWeekKey,
		headerIndex: Int,
		take: EvaluationArrival?
	) {
		val isPending = take != null && take !== positionedArrival.value
		val cardIndex = take?.takeIf { isPending }?.let { pending ->
			weekGroups.cardIndexIn(weekKey, pending.evaluationId, headerIndex)
		}

		if (take != null && isPending && cardIndex == null) {
			// The week shown does not list it: nothing to bring into view, and the week is positioned below.
			positionedArrival.value = take
		}

		if (take != null && isPending && cardIndex != null) {
			isProgrammaticWeekScroll.value = true
			try {
				withTimeoutOrNull(ARRIVAL_POSITION_TIMEOUT_MILLIS) {
					lazyListState.scrollToArrival(weekHeaderIndex = headerIndex, cardIndex = cardIndex)
				}
				hasPositionedSelectedWeek.value = true
			} finally {
				isProgrammaticWeekScroll.value = false
			}
			positionedArrival.value = take
			highlightedArrival.value = take
		} else if (!lazyListState.isScrollInProgress && lazyListState.firstVisibleItemIndex != headerIndex) {
			isProgrammaticWeekScroll.value = true
			try {
				if (hasPositionedSelectedWeek.value) {
					lazyListState.animateScrollToItem(headerIndex)
				} else {
					lazyListState.scrollToItem(headerIndex)
					hasPositionedSelectedWeek.value = true
				}
			} finally {
				isProgrammaticWeekScroll.value = false
			}
		} else {
			hasPositionedSelectedWeek.value = true
		}
	}
}

// Brings the arrival into view. The week header goes to the top when the whole card fits under it;
// otherwise the card goes right under the header, which stays pinned while the card is read.
private suspend fun LazyListState.scrollToArrival(weekHeaderIndex: Int, cardIndex: Int) {
	scrollToItem(index = weekHeaderIndex)
	snapshotFlow { firstVisibleItemIndex == weekHeaderIndex && firstVisibleItemScrollOffset == 0 }
		.first { isAtTop -> isAtTop }

	val header = layoutInfo.visibleItemsInfo.firstOrNull { item -> item.index == weekHeaderIndex }
	val card = layoutInfo.visibleItemsInfo.firstOrNull { item -> item.index == cardIndex }

	if (header == null) {
		return
	}

	if (card != null) {
		if (card.offset + card.size > layoutInfo.viewportSize.height) {
			scroll { scrollBy((card.offset - header.size).toFloat()) }
		}
	} else {
		// Not laid out under the header, so it lies beyond the viewport: it is brought to the top, then up by
		// the header's height, so that it sits under the pinned header. The end of the list can stop the first
		// step short; the card is visible by then either way, which is what counts.
		scrollToItem(index = cardIndex)
		snapshotFlow { layoutInfo.visibleItemsInfo.any { item -> item.index == cardIndex } }
			.first { isVisible -> isVisible }
		scroll { scrollBy(-header.size.toFloat()) }
	}
}

// The height the week between [lastWeek] (its header and its cards) takes, once every one of its
// items is laid out; null while any of them is not.
private fun LazyListState.laidOutHeightOf(lastWeek: IntRange): Int? {
	val laidOut = layoutInfo.visibleItemsInfo.filter { item -> item.index in lastWeek }

	return if (laidOut.size == lastWeek.last - lastWeek.first + 1) {
		laidOut.sumOf { item -> item.size }
	} else {
		null
	}
}

// The index of the card of [evaluationId] in the list, when the week it is filed under lists it.
private fun List<EvaluationsWeekGroupItem>.cardIndexIn(
	weekKey: EvaluationsWeekKey,
	evaluationId: String,
	headerIndex: Int
): Int? {
	val position = firstOrNull { weekGroup -> weekGroup.key == weekKey }
		?.evaluationItems()
		?.indexOfFirst { evaluation -> evaluation.evaluationId == evaluationId }
		?: -1

	return if (position >= 0) headerIndex + 1 + position else null
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

// The indexes the last week with evaluations takes: its header and then its cards.
private fun List<EvaluationsWeekGroupItem>.lastWeekIndexes(): IntRange? {
	var index = 0
	var lastWeek: IntRange? = null

	forEach { weekGroup ->
		val evaluationCount = weekGroup.evaluationItems().size

		if (evaluationCount > 0) {
			lastWeek = index..(index + evaluationCount)
			index += 1 + evaluationCount
		}
	}

	return lastWeek
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

// Reports the week the user scrolls to, so that the selection follows it. The list's own moves are not reported.
private suspend fun reportUserWeek(
	lazyListState: LazyListState,
	weekHeaderIndexes: Map<EvaluationsWeekKey, Int>,
	selectedWeekKey: EvaluationsWeekKey,
	isProgrammaticWeekScroll: MutableState<Boolean>,
	onVisibleWeekChange: (EvaluationsWeekKey) -> Unit
) {
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

// Long enough for any scroll to a week or a card to land; past it the highlight is started where the
// list stands rather than left waiting.
private const val ARRIVAL_POSITION_TIMEOUT_MILLIS = 2_000L
private val EvaluationsListBottomPadding = 120.dp
private const val EvaluationItemContentType = "evaluation_item"
private const val LAST_WEEK_TAIL_KEY = "evaluations_list_tail"
