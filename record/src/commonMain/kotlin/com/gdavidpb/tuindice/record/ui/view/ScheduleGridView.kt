package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDayItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleGridItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.label

private val BlockColumnWidth = 18.dp
private val BlockRowHeight = 32.dp
private val BlockRowGap = TuIndiceSpacing.Two

// Every meeting is this wide, enough for a subject code and its classroom to read whole. A day is as
// wide as the meetings it has side by side, so two subjects that clash each keep a whole cell, and a
// week that does not fit the screen scrolls sideways instead of squeezing.
private val LaneWidth = 72.dp

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
	val gridHeight = BlockRowHeight * grid.blockCount + BlockRowGap * (grid.blockCount - 1)
	// One state for the day names and the cells under them, so both move sideways together.
	val daysScrollState = rememberScrollState()

	Column(
		modifier = modifier.testTag(RecordUiTags.ScheduleGrid),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		Row(
			modifier = Modifier
				.padding(start = BlockColumnWidth)
				.horizontalScroll(daysScrollState)
		) {
			grid.days.forEach { dayItem ->
				Text(
					modifier = Modifier.width(LaneWidth * dayItem.laneCount),
					text = dayItem.day.label(isShort = true),
					style = MaterialTheme.typography.labelMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					textAlign = TextAlign.Center,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
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
							.width(LaneWidth * dayItem.laneCount)
							.height(gridHeight),
						dayItem = dayItem
					)
				}
			}
		}
	}
}

// The block numbers, each centered on its row and kept clear of the first day's cells.
@Composable
private fun ScheduleBlockColumn(blockCount: Int) {
	Column(
		modifier = Modifier.width(BlockColumnWidth),
		verticalArrangement = Arrangement.spacedBy(BlockRowGap)
	) {
		repeat(blockCount) { index ->
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.height(BlockRowHeight)
					.padding(end = TuIndiceSpacing.XSmall),
				contentAlignment = Alignment.CenterEnd
			) {
				Text(
					text = (index + 1).toString(),
					style = MaterialTheme.typography.labelSmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					textAlign = TextAlign.End,
					maxLines = 1
				)
			}
		}
	}
}

@Composable
private fun ScheduleDayColumn(
	modifier: Modifier,
	dayItem: ScheduleDayItem
) {
	Box(modifier = modifier) {
		dayItem.cells.forEach { cell ->
			val top = (BlockRowHeight + BlockRowGap) * (cell.startBlock - 1)
			val height = BlockRowHeight * cell.blockSpan + BlockRowGap * (cell.blockSpan - 1)

			ScheduleCellView(
				modifier = Modifier
					.offset(x = LaneWidth * cell.lane, y = top)
					.width(LaneWidth)
					.height(height)
					.padding(horizontal = TuIndiceSpacing.Hairline),
				day = dayItem.day,
				cell = cell
			)
		}
	}
}

// How many meetings the day has side by side at its busiest.
private val ScheduleDayItem.laneCount: Int
	get() = cells.maxOfOrNull { cell -> cell.laneCount } ?: 1
