package com.gdavidpb.tuindice.record.presentation.model

// The current term's schedule, laid out twice from the same meetings: the weekly grid and the
// table of one row per subject.
data class ScheduleItem(
	val grid: ScheduleGridItem,
	val table: ScheduleTableItem
)
