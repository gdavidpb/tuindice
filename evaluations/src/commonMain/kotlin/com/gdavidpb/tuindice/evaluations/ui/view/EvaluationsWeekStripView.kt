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
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

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

	LaunchedEffect(weekItems, selectedWeekIndex) {
		if (pagerState.currentPage != selectedWeekIndex) {
			pagerState.animateScrollToPage(selectedWeekIndex)
		}
	}

	LaunchedEffect(pagerState, weekItems, selectedWeekKey) {
		snapshotFlow { pagerState.isScrollInProgress to pagerState.currentPage }
			.distinctUntilChanged()
			.filter { (isScrollInProgress, _) -> !isScrollInProgress }
			.collect { (_, page) ->
				val weekKey = weekItems.getOrNull(page)?.key ?: return@collect

				if (weekKey != selectedWeekKey) {
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
			selectedItemKey = visibleSelectedWeekKey,
			itemKey = { item -> item.key },
			itemTestTag = { item -> EvaluationsUiTags.evaluationsWeekChip(item.key) },
			itemWidthFraction = WeekSelectorItemWidthFraction,
			onItemSelected = { item -> onWeekSelected(item.key) },
			contentType = WeekSelectorItemContentType
		) { item, isSelected ->
			EvaluationWeekSelectorItemView(
				item = item,
				isSelected = isSelected,
				showCurrentIndicator = false
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
