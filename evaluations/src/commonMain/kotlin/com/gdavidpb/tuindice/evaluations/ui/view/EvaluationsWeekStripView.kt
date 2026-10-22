package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.view.PeekingSelectorView
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance

@Composable
fun EvaluationsWeekStripView(
	items: List<EvaluationsWeekItem>,
	selectedWeekKey: EvaluationsWeekKey,
	onWeekSelected: (EvaluationsWeekKey) -> Unit,
	modifier: Modifier = Modifier
) {
	val weekItems = remember(items) {
		items.sortedBy { item -> item.key.sortOrder }
	}
	val selectedWeekIndex = remember(weekItems, selectedWeekKey) {
		weekItems.indexOfFirst { item -> item.key == selectedWeekKey }
			.takeIf { index -> index >= 0 }
			?: 0
	}
	val pagerState = rememberPagerState(
		initialPage = selectedWeekIndex,
		pageCount = { weekItems.size }
	)
	val visibleSelectedWeekKey = weekItems
		.getOrNull(pagerState.currentPage)
		?.key
		?: selectedWeekKey

	// Set by a drag of the page and taken by the page it settles on. A move the strip makes itself clears it, so the
	// page it settles on is not reported: only a page the student dragged to chooses its week. A drag that interrupts
	// the strip's own move still chooses its week: dragging the strip is choosing a week on purpose.
	val isMovedByDrag = remember { mutableStateOf(false) }

	LaunchedEffect(pagerState) {
		pagerState.interactionSource.interactions
			.filterIsInstance<DragInteraction.Start>()
			.collect { isMovedByDrag.value = true }
	}

	LaunchedEffect(weekItems, selectedWeekIndex) {
		if (pagerState.currentPage != selectedWeekIndex) {
			isMovedByDrag.value = false
			pagerState.animateScrollToPage(selectedWeekIndex)
		}
	}

	LaunchedEffect(pagerState, weekItems, selectedWeekKey) {
		snapshotFlow { pagerState.isScrollInProgress to pagerState.currentPage }
			.distinctUntilChanged()
			.filter { (isScrollInProgress, _) -> !isScrollInProgress }
			.collect { (_, page) ->
				val weekKey = weekItems.getOrNull(page)?.key ?: return@collect
				val isDraggedTo = isMovedByDrag.value

				isMovedByDrag.value = false

				if (isDraggedTo && weekKey != selectedWeekKey) {
					onWeekSelected(weekKey)
				}
			}
	}

	Column(
		modifier = modifier
			.testTag(EvaluationsUiTags.EvaluationsWeekStrip)
			.fillMaxWidth()
			.padding(top = 4.dp, bottom = 10.dp)
	) {
		PeekingSelectorView(
			items = weekItems,
			selectedItemKey = visibleSelectedWeekKey.tagSuffix,
			itemKey = { item -> item.key.tagSuffix },
			itemTestTag = { item -> EvaluationsUiTags.evaluationsWeekChip(item.key) },
			itemWidthFraction = WeekSelectorItemWidthFraction,
			onItemSelected = { item -> onWeekSelected(item.key) },
			contentType = WeekSelectorItemContentType
		) { item, isSelected ->
			EvaluationWeekSelectorItemView(
				item = item,
				isSelected = isSelected
			)
		}

		HorizontalPager(
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 12.dp),
			state = pagerState,
			key = { page -> weekItems[page].key.tagSuffix }
		) { page ->
			val item = weekItems[page]

			Row(
				modifier = Modifier
					.testTag(EvaluationsUiTags.evaluationsWeekPage(item.key))
					.fillMaxWidth()
					.padding(start = 24.dp, end = 24.dp)
			) {
				item.days.forEach { day ->
					EvaluationWeekDayView(
						modifier = Modifier.weight(1f),
						item = day
					)
				}
			}
		}
	}
}

private const val WeekSelectorItemWidthFraction = 0.40f
private const val WeekSelectorItemContentType = "week_selector_item"
