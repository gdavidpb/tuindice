package com.gdavidpb.tuindice.record.ui.model

import androidx.compose.ui.unit.Dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDayItem

/**
 * The stretches of today's column where the "now" line runs over a meeting, measured from the left
 * edge of the column: the cells whose blocks hold the current one, each on its lane. Over them the
 * line takes the ink that reads on a subject's colour; outside them, the one of the sheet.
 */
fun ScheduleDayItem.nowLineCellSpans(): List<ClosedRange<Dp>> {
	val offset = nowBlockOffset ?: return emptyList()
	val nowBlock = offset.toInt() + 1

	return cells
		.filter { cell -> nowBlock in cell.startBlock..cell.endBlock }
		.map { cell ->
			val left = ScheduleGridDefaults.LaneWidth * cell.lane + TuIndiceSpacing.Hairline

			left..(left + ScheduleGridDefaults.LaneWidth - TuIndiceSpacing.Hairline * 2)
		}
}
