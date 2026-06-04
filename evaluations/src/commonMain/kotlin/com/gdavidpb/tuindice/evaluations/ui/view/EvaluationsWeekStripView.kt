package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

	HorizontalPager(
		modifier = modifier
			.testTag(EvaluationsUiTags.EvaluationsWeekStrip)
			.fillMaxWidth(),
		state = pagerState,
		key = { page -> weekItems[page].weekNumber }
	) { page ->
		val item = weekItems[page]

		Column(
			modifier = Modifier
				.testTag(EvaluationsUiTags.evaluationsWeekPage(item.weekNumber))
				.fillMaxWidth()
				.padding(top = 4.dp, bottom = 10.dp)
		) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 24.dp),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(10.dp)
			) {
				RowDivider(modifier = Modifier.weight(1f))

				Icon(
					modifier = Modifier.size(18.dp),
					imageVector = Icons.Outlined.CalendarToday,
					tint = MaterialTheme.colorScheme.primary,
					contentDescription = null
				)

				Text(
					modifier = Modifier.testTag(EvaluationsUiTags.EvaluationsWeekLabel),
					text = item.labelText,
					color = MaterialTheme.colorScheme.primary,
					style = MaterialTheme.typography.labelLarge,
					fontWeight = FontWeight.Bold
				)

				RowDivider(modifier = Modifier.weight(1f))
			}

			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 14.dp, start = 24.dp, end = 24.dp)
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

@Composable
private fun RowDivider(
	modifier: Modifier = Modifier
) {
	androidx.compose.foundation.layout.Box(
		modifier = modifier
			.height(1.dp)
			.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.55f))
	)
}
