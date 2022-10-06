package com.gdavidpb.tuindice.base.domain.model

data class ScheduleEntry(
	val dayOfWeek: Int,
	val startAt: Int,
	val endAt: Int,
	val classroom: String
)