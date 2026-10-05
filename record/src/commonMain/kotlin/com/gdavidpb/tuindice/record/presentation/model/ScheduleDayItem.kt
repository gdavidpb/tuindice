package com.gdavidpb.tuindice.record.presentation.model

data class ScheduleDayItem(
	val day: ScheduleDay,
	val cells: List<ScheduleCellItem>
)
