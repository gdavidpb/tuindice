package com.gdavidpb.tuindice.record.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// The class schedule of the current term, ready to lay out. Withdrawn subjects are not in it, and
// the subjects whose schedule is still to be agreed are named in one line under it.
data class ScheduleGridItem(
	val blockCount: Int,
	val days: List<ScheduleDayItem>,
	// "Sin horario: EP1420, CI2691", or null when every subject has a meeting placed.
	val unscheduledText: UiText?
)
