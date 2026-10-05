package com.gdavidpb.tuindice.record.presentation.model

// The class schedule of the current term, ready to lay out. Withdrawn subjects are not in it, and
// the subjects whose schedule is still to be agreed are listed by code.
data class ScheduleGridItem(
	val blockCount: Int,
	val days: List<ScheduleDayItem>,
	val unscheduledCodes: List<String>
)
