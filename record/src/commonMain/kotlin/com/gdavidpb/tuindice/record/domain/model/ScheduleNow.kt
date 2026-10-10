package com.gdavidpb.tuindice.record.domain.model

/**
 * The moment the schedule is being looked at, in the device's local time. [dayOfWeek] uses the
 * backend's `day_of_week` (1 = Sunday ... 7 = Saturday), the same code a meeting carries.
 */
data class ScheduleNow(
	val dayOfWeek: Int,
	val minuteOfDay: Int
)
