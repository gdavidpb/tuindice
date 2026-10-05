package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDayItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleGridItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.label

private val BlockColumnWidth = 18.dp
private val BlockRowHeight = 32.dp
private val BlockRowGap = TuIndiceSpacing.Two

/**
 * Weekly grid of the university's class blocks: the block numbers on the left, one column per day
 * and each meeting as a cell spanning its blocks. Overlapping meetings share the width of the day.
 */
@Composable
fun ScheduleGridView(
	modifier: Modifier = Modifier,
	grid: ScheduleGridItem
) {
	val gridHeight = BlockRowHeight * grid.blockCount + BlockRowGap * (grid.blockCount - 1)

	Column(
		modifier = modifier.testTag(RecordUiTags.ScheduleGrid),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		Row(modifier = Modifier.fillMaxWidth()) {
			Spacer(modifier = Modifier.width(BlockColumnWidth))

			grid.days.forEach { dayItem ->
				Text(
					modifier = Modifier.weight(1f),
					text = dayItem.day.label(isShort = true),
					style = MaterialTheme.typography.labelMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					textAlign = TextAlign.Center,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
			}
		}

		Row(modifier = Modifier.fillMaxWidth()) {
			Column(
				modifier = Modifier.width(BlockColumnWidth),
				verticalArrangement = Arrangement.spacedBy(BlockRowGap)
			) {
				repeat(grid.blockCount) { index ->
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.height(BlockRowHeight),
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

			grid.days.forEach { dayItem ->
				ScheduleDayColumn(
					modifier = Modifier
						.weight(1f)
						.height(gridHeight),
					dayItem = dayItem
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
	BoxWithConstraints(modifier = modifier) {
		dayItem.cells.forEach { cell ->
			val laneWidth: Dp = maxWidth / cell.laneCount
			val top = (BlockRowHeight + BlockRowGap) * (cell.startBlock - 1)
			val height = BlockRowHeight * cell.blockSpan + BlockRowGap * (cell.blockSpan - 1)

			ScheduleCellView(
				modifier = Modifier
					.offset(x = laneWidth * cell.lane, y = top)
					.width(laneWidth)
					.height(height)
					.padding(horizontal = TuIndiceSpacing.Hairline),
				day = dayItem.day,
				cell = cell
			)
		}
	}
}
