package com.gdavidpb.tuindice.record.presentation.model

private const val SUNDAY_CODE = 1
private const val MONDAY_CODE = 2
private const val TUESDAY_CODE = 3
private const val WEDNESDAY_CODE = 4
private const val THURSDAY_CODE = 5
private const val FRIDAY_CODE = 6
private const val SATURDAY_CODE = 7

/**
 * Days of the schedule grid in display order. [code] is the backend's `day_of_week`
 * (1 = Sunday ... 7 = Saturday).
 */
enum class ScheduleDay(val code: Int) {
	Monday(MONDAY_CODE),
	Tuesday(TUESDAY_CODE),
	Wednesday(WEDNESDAY_CODE),
	Thursday(THURSDAY_CODE),
	Friday(FRIDAY_CODE),
	Saturday(SATURDAY_CODE),
	Sunday(SUNDAY_CODE);

	val isWeekend: Boolean
		get() = this == Saturday || this == Sunday

	companion object {
		fun fromCode(code: Int): ScheduleDay? = entries.firstOrNull { day -> day.code == code }
	}
}
