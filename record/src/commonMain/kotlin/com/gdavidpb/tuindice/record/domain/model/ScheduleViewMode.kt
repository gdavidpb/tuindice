package com.gdavidpb.tuindice.record.domain.model

/** How the class schedule is drawn: one row per subject, or the weekly grid of blocks. */
enum class ScheduleViewMode(
	val storageValue: String
) {
	Table("table"),
	Week("week");

	companion object {
		// The table is what the university's enrollment proof looks like, so it is where a first visit lands.
		fun fromStorageValue(value: String?): ScheduleViewMode {
			return entries.firstOrNull { mode -> mode.storageValue == value }
				?: Table
		}
	}
}
