package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.view.PeekingSelectorView
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekItem
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Composable
fun EvaluationsWeekStripView(
	items: List<EvaluationsWeekItem>,
	selectedWeekNumber: Int,
	onWeekSelected: (Int) -> Unit,
	modifier: Modifier = Modifier
) {
	val weekItems = remember(items) {
		items.sortedBy { item -> item.weekNumber }
	}
	val selectedWeekIndex = remember(weekItems, selectedWeekNumber) {
		weekItems.indexOfFirst { item -> item.weekNumber == selectedWeekNumber }
			.takeIf { index -> index >= 0 }
			?: 0
	}
	val pagerState = rememberPagerState(
		initialPage = selectedWeekIndex,
		pageCount = { weekItems.size }
	)
	val visibleSelectedWeekNumber = weekItems
		.getOrNull(pagerState.currentPage)
		?.weekNumber
		?: selectedWeekNumber

	LaunchedEffect(weekItems, selectedWeekIndex) {
		if (pagerState.currentPage != selectedWeekIndex) {
			pagerState.animateScrollToPage(selectedWeekIndex)
		}
	}

	LaunchedEffect(pagerState, weekItems, selectedWeekNumber) {
		snapshotFlow { pagerState.isScrollInProgress to pagerState.currentPage }
			.distinctUntilChanged()
			.filter { (isScrollInProgress, _) -> !isScrollInProgress }
			.collect { (_, page) ->
				val weekNumber = weekItems.getOrNull(page)?.weekNumber ?: return@collect

				if (weekNumber != selectedWeekNumber) {
					onWeekSelected(weekNumber)
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
			selectedItemKey = visibleSelectedWeekNumber,
			itemKey = { item -> item.weekNumber },
			itemTestTag = { item -> EvaluationsUiTags.evaluationsWeekChip(item.weekNumber) },
			onItemSelected = { item -> onWeekSelected(item.weekNumber) },
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
			key = { page -> weekItems[page].weekNumber }
		) { page ->
			val item = weekItems[page]

			Row(
				modifier = Modifier
					.testTag(EvaluationsUiTags.evaluationsWeekPage(item.weekNumber))
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

private const val WeekSelectorItemContentType = "week_selector_item"
