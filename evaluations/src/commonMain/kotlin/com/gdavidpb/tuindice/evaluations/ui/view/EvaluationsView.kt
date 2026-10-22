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
import com.gdavidpb.tuindice.evaluations.ui.model.EvaluationItemActions
import com.gdavidpb.tuindice.evaluations.ui.model.EvaluationSwipeActions
import com.gdavidpb.tuindice.evaluations.ui.model.EvaluationsActions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
	actions: EvaluationsActions,
	scrollEnabled: Boolean = true,
	openActionsEvaluationId: String? = null,
	focusEvaluationId: String? = null,
	onFocusEvaluationBoundsChange: (Rect?) -> Unit = {},
	arrivalEvaluationId: String? = null
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
	// The week the list stands on, once it has been placed there. Until then the list does not choose a week.
	val positionedWeek = remember { mutableStateOf<EvaluationsWeekKey?>(null) }

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
			positionedWeek = positionedWeek,
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
		actions.onEvaluationArrivalTaken(evaluationId)
	}

	LaunchedEffect(selectedWeekKey, weekHeaderIndexes, arrival.value) {
		positioning.placeSelection(
			weekGroups = weekGroups,
			weekKey = selectedWeekKey,
			weekHeaderIndexes = weekHeaderIndexes,
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

	LaunchedEffect(lazyListState, weekHeaderIndexes) {
		reportUserWeek(
			positioning = positioning,
			weekHeaderIndexes = weekHeaderIndexes,
			onVisibleWeekChange = actions.onWeekClick
		)
	}

	// A week left unplaced by a gesture, or by a placement that the gesture cut short, is placed once the gesture ends.
	LaunchedEffect(lazyListState, weekGroups, weekHeaderIndexes, selectedWeekKey) {
		snapshotFlow { lazyListState.isScrollInProgress }
			.filter { isScrollInProgress -> !isScrollInProgress }
			.collect {
				positioning.placeUnplaced(
					weekGroups = weekGroups,
					weekKey = selectedWeekKey,
					weekHeaderIndexes = weekHeaderIndexes,
					take = arrival.value
				)
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
				val highlight = highlightedArrival.value?.takeIf { highlighted ->
					highlighted.evaluationId == evaluation.evaluationId
				}

				val askAction = assistantAskAction(
					onAskAssistant = actions.onAskAssistant,
					context = evaluation.assistantContext
				)

				EvaluationSwipeToDismiss(
					modifier = itemModifier.animateItem(
						fadeInSpec = null,
						fadeOutSpec = null
					),
					initiallyOpen = evaluation.evaluationId == openActionsEvaluationId,
					actions = EvaluationSwipeActions(
						onAsk = askAction,
						onEdit = { actions.onEvaluationEdit(evaluation.evaluationId) },
						onDelete = { actions.onEvaluationDelete(evaluation.evaluationId) }
					)
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
						actions = EvaluationItemActions(
							onGradeClick = {
								if (evaluation.isClickable) {
									actions.onEvaluationClick(
										evaluation.evaluationId,
										evaluation.nameText,
										evaluation.subjectCodeText
									)
								}
							},
							onCardClick = onActionsClick,
							onEditAction = { actions.onEvaluationEdit(evaluation.evaluationId) },
							onDeleteAction = { actions.onEvaluationDelete(evaluation.evaluationId) },
							onAskAssistant = askAction
						)
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
	val positionedWeek: MutableState<EvaluationsWeekKey?>,
	val positionedArrival: ArrivalCursor,
	val highlightedArrival: MutableState<EvaluationArrival?>
) {
	// One placement at a time, so a placement and the check after a gesture never move the list together.
	private val placements = Mutex()

	// Whether the list stands on [weekKey] already, with the arrival taken in it when there is one.
	fun isPlaced(weekKey: EvaluationsWeekKey, take: EvaluationArrival?): Boolean {
		return positionedWeek.value == weekKey && (take == null || take === positionedArrival.value)
	}

	// Places the list on the week the selection moved to. A selection that the student's gesture made is placed already.
	suspend fun placeSelection(
		weekGroups: List<EvaluationsWeekGroupItem>,
		weekKey: EvaluationsWeekKey,
		weekHeaderIndexes: Map<EvaluationsWeekKey, Int>,
		take: EvaluationArrival?
	) {
		if (!isPlaced(weekKey, take)) {
			positionedWeek.value = null
			place(weekGroups, weekKey, weekHeaderIndexes, take)
		}
	}

	// Places the week that a gesture left unplaced, once the gesture has ended.
	suspend fun placeUnplaced(
		weekGroups: List<EvaluationsWeekGroupItem>,
		weekKey: EvaluationsWeekKey,
		weekHeaderIndexes: Map<EvaluationsWeekKey, Int>,
		take: EvaluationArrival?
	) {
		if (positionedWeek.value == null) {
			place(weekGroups, weekKey, weekHeaderIndexes, take)
		}
	}

	// Places the list on [weekKey] and, when an arrival is pending in it, on its evaluation. While a gesture moves the
	// list nothing is placed: the week is placed once the gesture ends. A gesture that cuts a placement short leaves the
	// week unplaced in the same way.
	private suspend fun place(
		weekGroups: List<EvaluationsWeekGroupItem>,
		weekKey: EvaluationsWeekKey,
		weekHeaderIndexes: Map<EvaluationsWeekKey, Int>,
		take: EvaluationArrival?
	) {
		placements.withLock {
			// A placement that waited for another one finds the week placed already, and a gesture moves the list itself.
			val isSettled = isPlaced(weekKey, take) || lazyListState.isScrollInProgress

			if (!isSettled) {
				placeWeekAt(weekGroups, weekKey, weekHeaderIndexes, weekHeaderIndexes[weekKey], take)
			}
		}
	}

	private suspend fun placeWeekAt(
		weekGroups: List<EvaluationsWeekGroupItem>,
		weekKey: EvaluationsWeekKey,
		weekHeaderIndexes: Map<EvaluationsWeekKey, Int>,
		headerIndex: Int?,
		take: EvaluationArrival?
	) {
		if (headerIndex == null) {
			// The week lists no evaluation: there is nothing to place, so the list stays where it stands.
			positionedWeek.value = weekKey
		} else {
			placeOnHeader(weekGroups, weekKey, weekHeaderIndexes, headerIndex, take)
		}
	}

	// Scrolls the list onto the header of the week, and onto the arrival's card when one is pending in it. The week is
	// placed only when the scroll was not cut short and the list stands on the week when it ends.
	private suspend fun placeOnHeader(
		weekGroups: List<EvaluationsWeekGroupItem>,
		weekKey: EvaluationsWeekKey,
		weekHeaderIndexes: Map<EvaluationsWeekKey, Int>,
		headerIndex: Int,
		take: EvaluationArrival?
	) {
		val isArrivalPending = take != null && take !== positionedArrival.value
		val cardIndex = take?.takeIf { isArrivalPending }?.let { pending ->
			weekGroups.cardIndexIn(weekKey, pending.evaluationId, headerIndex)
		}
		val isScrolled = scrollToWeek(headerIndex, cardIndex)

		if (isScrolled && weekHeaderIndexes.visibleWeekKey(lazyListState.firstVisibleItemIndex) == weekKey) {
			if (isArrivalPending) {
				positionedArrival.value = take

				if (cardIndex != null) {
					highlightedArrival.value = take
				}
			}

			positionedWeek.value = weekKey
		}
	}

	// Scrolls the list onto the week's header, or onto the arrival's card. Returns false when a gesture cut the scroll
	// short: the week stays unplaced, and it is placed once the gesture ends.
	private suspend fun scrollToWeek(headerIndex: Int, cardIndex: Int?): Boolean {
		isProgrammaticWeekScroll.value = true

		return try {
			if (cardIndex != null) {
				withTimeoutOrNull(ARRIVAL_POSITION_TIMEOUT_MILLIS) {
					lazyListState.scrollToArrival(weekHeaderIndex = headerIndex, cardIndex = cardIndex)
				}
				hasPositionedSelectedWeek.value = true
			} else if (lazyListState.firstVisibleItemIndex != headerIndex) {
				if (hasPositionedSelectedWeek.value) {
					lazyListState.animateScrollToItem(headerIndex)
				} else {
					lazyListState.scrollToItem(headerIndex)
					hasPositionedSelectedWeek.value = true
				}
			}

			true
		} catch (expectedCancellation: CancellationException) {
			// A gesture moved the list and cut the scroll short. Any other cancellation belongs to the caller.
			currentCoroutineContext().ensureActive()
			false
		} finally {
			isProgrammaticWeekScroll.value = false
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

// Reports the week that a gesture of the student moves the list to, so that the selection follows it. The list's own
// moves are not reported, and neither is a gesture on a list that is not placed yet: the placement after the gesture
// decides. A report places the list on the week it reports, so the selection never goes back to the week it left.
private suspend fun reportUserWeek(
	positioning: WeekPositioning,
	weekHeaderIndexes: Map<EvaluationsWeekKey, Int>,
	onVisibleWeekChange: (EvaluationsWeekKey) -> Unit
) {
	snapshotFlow { positioning.lazyListState.firstVisibleItemIndex }
		.map { index -> weekHeaderIndexes.visibleWeekKey(index) }
		.distinctUntilChanged()
		.collect { weekKey ->
			val placedWeek = positioning.positionedWeek.value

			if (weekKey == null || placedWeek == null) {
				return@collect
			}

			val isGestureMove = positioning.lazyListState.isScrollInProgress &&
				!positioning.isProgrammaticWeekScroll.value

			if (isGestureMove && weekKey != placedWeek) {
				positioning.positionedWeek.value = weekKey
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
