package com.gdavidpb.tuindice.record.presentation.model

// One column per day, the same days as the grid, and one row per subject that is not withdrawn.
data class ScheduleTableItem(
	val days: List<ScheduleDay>,
	val rows: List<ScheduleTableRowItem>
)
