package com.gdavidpb.tuindice.record.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.TermProjection
import com.gdavidpb.tuindice.base.domain.model.ScheduleNow

data class ObservedSchedule(
	// The term being lived, or null while the record has none.
	val currentTerm: TermProjection?,
	val viewMode: ScheduleViewMode,
	val hasSyncedRecord: Boolean,
	// The moment being looked at: what places today, the "now" line and the class in progress.
	val now: ScheduleNow,
	// The week of the term [now] falls in (1..12), or null when it falls outside the term's weeks or
	// there is no term.
	val academicWeek: Int?,
	// The meetings with an evaluation this calendar week. Empty while the evaluations are not
	// downloaded or cannot be read: the schedule is then drawn as it would be without them.
	val evaluationMarks: ScheduleEvaluationMarks
)
