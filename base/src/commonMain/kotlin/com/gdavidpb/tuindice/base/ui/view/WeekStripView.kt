package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.model.WeekStripDayItem
import com.gdavidpb.tuindice.base.ui.model.WeekStripItem
import com.gdavidpb.tuindice.base.ui.model.WeekStripTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance

// The strip of weeks: the weeks in a row, the one on screen in the middle and the others peeking at its sides,
// and under it the days of the week on screen. Dragging the strip to a week chooses it; a move the strip makes itself
// does not. The items come in the order they are shown in.
@Composable
fun <K> WeekStripView(
	items: List<WeekStripItem<K>>,
	selectedKey: K,
	onKeySelected: (K) -> Unit,
	tags: WeekStripTags<K>,
	modifier: Modifier = Modifier
) {
	val selectedWeekIndex = remember(items, selectedKey) {
		items.indexOfFirst { item -> item.key == selectedKey }
			.takeIf { index -> index >= 0 }
			?: 0
	}
	val pagerState = rememberPagerState(
		initialPage = selectedWeekIndex,
		pageCount = { items.size }
	)
	// The week the strip stands on. Before the pager has a page of its own, it is the selected week, if the list has it.
	val visibleTagSuffix = items.getOrNull(pagerState.currentPage)?.tagSuffix
		?: items.firstOrNull { item -> item.key == selectedKey }?.tagSuffix

	// Set by a drag of the page and taken by the page it settles on. A move the strip makes itself clears it, so the
	// page it settles on is not reported: only a page the student dragged to chooses its week. A drag that interrupts
	// the strip's own move still chooses its week: dragging the strip is choosing a week on purpose.
	val isMovedByDrag = remember { mutableStateOf(false) }

	LaunchedEffect(pagerState) {
		pagerState.interactionSource.interactions
			.filterIsInstance<DragInteraction.Start>()
			.collect { isMovedByDrag.value = true }
	}

	LaunchedEffect(items, selectedWeekIndex) {
		if (pagerState.currentPage != selectedWeekIndex) {
			isMovedByDrag.value = false
			pagerState.animateScrollToPage(selectedWeekIndex)
		}
	}

	LaunchedEffect(pagerState, items, selectedKey) {
		snapshotFlow { pagerState.isScrollInProgress to pagerState.currentPage }
			.distinctUntilChanged()
			.filter { (isScrollInProgress, _) -> !isScrollInProgress }
			.collect { (_, page) ->
				val weekKey = items.getOrNull(page)?.key ?: return@collect
				val isDraggedTo = isMovedByDrag.value

				isMovedByDrag.value = false

				if (isDraggedTo && weekKey != selectedKey) {
					onKeySelected(weekKey)
				}
			}
	}

	Column(
		modifier = modifier
			.testTag(tags.strip)
			.fillMaxWidth()
			.padding(top = TuIndiceSpacing.XSmall, bottom = TuIndiceSpacing.Large)
	) {
		WeekStripPeek(
			items = items,
			visibleTagSuffix = visibleTagSuffix,
			tags = tags,
			onKeySelected = onKeySelected
		)

		WeekStripPages(
			items = items,
			pagerState = pagerState,
			tags = tags
		)
	}
}

// The weeks in their row. The week on screen is the selected one, and a tap on another week chooses it.
@Composable
private fun <K> WeekStripPeek(
	items: List<WeekStripItem<K>>,
	visibleTagSuffix: String?,
	tags: WeekStripTags<K>,
	onKeySelected: (K) -> Unit
) {
	PeekingSelectorView(
		items = items,
		selectedItemKey = visibleTagSuffix,
		itemKey = { item -> item.tagSuffix },
		itemTestTag = { item -> tags.chip(item.key) },
		itemWidthFraction = WEEK_SELECTOR_ITEM_WIDTH_FRACTION,
		onItemSelected = { item -> onKeySelected(item.key) },
		contentType = WEEK_SELECTOR_ITEM_CONTENT_TYPE
	) { item, isSelected ->
		WeekSelectorItemView(
			labelText = item.labelText,
			isCurrent = item.isCurrent,
			isSelected = isSelected,
			tags = tags.selector
		)
	}
}

// The days of each week, one page per week, so the strip moves from a week to the next with a swipe.
@Composable
private fun <K> WeekStripPages(
	items: List<WeekStripItem<K>>,
	pagerState: PagerState,
	tags: WeekStripTags<K>
) {
	HorizontalPager(
		modifier = Modifier
			.fillMaxWidth()
			.padding(top = TuIndiceSpacing.XLarge),
		state = pagerState,
		key = { page -> items[page].tagSuffix }
	) { page ->
		val item = items[page]

		WeekStripDays(
			days = item.days,
			pageTag = tags.page(item.key)
		)
	}
}

@Composable
private fun WeekStripDays(
	days: List<WeekStripDayItem>,
	pageTag: String
) {
	Row(
		modifier = Modifier
			.testTag(pageTag)
			.fillMaxWidth()
			.padding(horizontal = TuIndiceSpacing.Dialog)
	) {
		days.forEach { day ->
			WeekDayView(
				modifier = Modifier.weight(1f),
				item = day
			)
		}
	}
}

private const val WEEK_SELECTOR_ITEM_WIDTH_FRACTION = 0.40f
private const val WEEK_SELECTOR_ITEM_CONTENT_TYPE = "week_selector_item"
