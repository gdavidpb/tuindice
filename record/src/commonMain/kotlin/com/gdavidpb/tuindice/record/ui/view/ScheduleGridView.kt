package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.record.presentation.model.ScheduleGridItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.ScheduleGridDefaults

/**
 * Weekly grid of the university's class blocks: the block numbers on the left, one column per day
 * and each meeting as a cell of a fixed width spanning its blocks. It scrolls both ways, with the
 * day names and the block numbers staying in place.
 */
@Composable
fun ScheduleGridView(
	modifier: Modifier = Modifier,
	grid: ScheduleGridItem
) {
	val gridHeight = ScheduleGridDefaults.BlockRowHeight * grid.blockCount +
		ScheduleGridDefaults.BlockRowGap * (grid.blockCount - 1)
	// One state for the day names and the cells under them, so both move sideways together.
	val daysScrollState = rememberScrollState()

	Column(
		modifier = modifier.testTag(RecordUiTags.ScheduleGrid),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		Row(
			modifier = Modifier
				.padding(start = ScheduleGridDefaults.BlockColumnWidth)
				.horizontalScroll(daysScrollState)
		) {
			grid.days.forEach { dayItem ->
				ScheduleDayHeaderView(
					modifier = Modifier.width(ScheduleGridDefaults.LaneWidth * dayItem.laneCount),
					day = dayItem.day,
					isToday = dayItem.isToday
				)
			}
		}

		Row(
			modifier = Modifier
				.weight(1f, fill = false)
				.verticalScroll(rememberScrollState())
		) {
			ScheduleBlockColumn(blockCount = grid.blockCount)

			Row(modifier = Modifier.horizontalScroll(daysScrollState)) {
				grid.days.forEach { dayItem ->
					ScheduleDayColumn(
						modifier = Modifier
							.width(ScheduleGridDefaults.LaneWidth * dayItem.laneCount)
							.height(gridHeight),
						dayItem = dayItem
					)
				}
			}
		}
	}
}
