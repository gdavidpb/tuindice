package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDayItem
import com.gdavidpb.tuindice.record.ui.model.ScheduleGridDefaults

/**
 * One day of the weekly grid: each meeting placed at its blocks, on the lane the layout gave it.
 * Today's column carries a soft tint of the accent from top to bottom and, while the hour falls
 * inside the grid, the "now" line across it.
 */
@Composable
fun ScheduleDayColumn(
	modifier: Modifier = Modifier,
	dayItem: ScheduleDayItem
) {
	val tint = if (dayItem.isToday) {
		MaterialTheme.colorScheme.primary.copy(alpha = TuIndiceAlpha.SurfaceTint)
	} else {
		Color.Transparent
	}

	Box(modifier = modifier.background(tint)) {
		dayItem.cells.forEach { cell ->
			val top = (ScheduleGridDefaults.BlockRowHeight + ScheduleGridDefaults.BlockRowGap) * (cell.startBlock - 1)
			val height = ScheduleGridDefaults.BlockRowHeight * cell.blockSpan +
				ScheduleGridDefaults.BlockRowGap * (cell.blockSpan - 1)

			ScheduleCellView(
				modifier = Modifier
					.offset(x = ScheduleGridDefaults.LaneWidth * cell.lane, y = top)
					.width(ScheduleGridDefaults.LaneWidth)
					.height(height)
					.padding(horizontal = TuIndiceSpacing.Hairline),
				day = dayItem.day,
				cell = cell
			)
		}

		if (dayItem.nowBlockOffset != null) {
			// The blocks already gone by, with the gaps between them, plus the part of the current
			// one; drawn last so it stays over the cells, and centred on that height.
			val wholeBlocks = dayItem.nowBlockOffset.toInt()
			val nowY = (ScheduleGridDefaults.BlockRowHeight + ScheduleGridDefaults.BlockRowGap) * wholeBlocks +
				ScheduleGridDefaults.BlockRowHeight * (dayItem.nowBlockOffset - wholeBlocks)

			ScheduleNowLineView(
				modifier = Modifier
					.offset(y = nowY - ScheduleGridDefaults.NowDotSize / 2)
					.fillMaxWidth()
			)
		}
	}
}
