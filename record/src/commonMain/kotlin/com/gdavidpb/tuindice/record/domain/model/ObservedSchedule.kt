package com.gdavidpb.tuindice.record.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.TermProjection

data class ObservedSchedule(
	// The term being lived, or null while the record has none.
	val currentTerm: TermProjection?,
	val viewMode: ScheduleViewMode,
	val hasSyncedRecord: Boolean,
	// The moment being looked at: what places today, the "now" line and the class in progress.
	val now: ScheduleNow
)
