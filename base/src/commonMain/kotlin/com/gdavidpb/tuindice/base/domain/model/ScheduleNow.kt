package com.gdavidpb.tuindice.base.domain.model

import kotlinx.datetime.LocalDate

/**
 * The moment the schedule is being looked at, in the device's local time. [dayOfWeek] uses the
 * backend's `day_of_week` (1 = Sunday ... 7 = Saturday), the same code a meeting carries. [date] is
 * the local calendar date of the same instant, so a consumer that needs "today" does not have to
 * derive it again from a second clock.
 */
data class ScheduleNow(
	val dayOfWeek: Int,
	val minuteOfDay: Int,
	val date: LocalDate
)
