package com.gdavidpb.tuindice.record.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.TermProjection

data class ObservedSchedule(
	// The term being lived, or null while the record has none.
	val currentTerm: TermProjection?,
	val viewMode: ScheduleViewMode,
	val hasSyncedRecord: Boolean
)
