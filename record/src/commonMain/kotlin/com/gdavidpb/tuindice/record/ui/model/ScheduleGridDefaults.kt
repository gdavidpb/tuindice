package com.gdavidpb.tuindice.record.ui.model

import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// The measures the weekly grid, its block numbers and its day columns share, so a block number
// sits level with the cells of that block.
object ScheduleGridDefaults {
	val BlockColumnWidth = 18.dp
	val BlockRowHeight = 32.dp
	val BlockRowGap = TuIndiceSpacing.Two

	// Every meeting is this wide, enough for a subject code and its classroom to read whole. A day is
	// as wide as the meetings it has side by side, so two subjects that clash each keep a whole cell,
	// and a week that does not fit the screen scrolls sideways instead of squeezing.
	val LaneWidth = 72.dp

	// The "now" line: thick enough to read over a cell, with a dot marking where it starts.
	val NowLineThickness = TuIndiceSpacing.Two
	val NowDotSize = 8.dp
}
