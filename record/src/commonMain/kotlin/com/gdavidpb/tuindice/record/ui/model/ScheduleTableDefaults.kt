package com.gdavidpb.tuindice.record.ui.model

import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// The measures the schedule table's header, rows and cells share, so a day's name sits right over
// that day's cells.
object ScheduleTableDefaults {
	val DayWidth = 34.dp
	val CellGap = TuIndiceSpacing.Two
	val RowPadding = TuIndiceSpacing.Medium
}
