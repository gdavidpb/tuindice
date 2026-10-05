package com.gdavidpb.tuindice.record.presentation.model

data class ScheduleDayItem(
	val day: ScheduleDay,
	val cells: List<ScheduleCellItem>,
	val isToday: Boolean = false,
	// Where the "now" line crosses this day, in blocks from the top of block 1 (1.5 is halfway
	// through block 2). Only today has one, and only while the hour falls inside the grid.
	val nowBlockOffset: Float? = null
) {
	// How many meetings the day has side by side at its busiest.
	val laneCount: Int
		get() = cells.maxOfOrNull { cell -> cell.laneCount } ?: 1
}
