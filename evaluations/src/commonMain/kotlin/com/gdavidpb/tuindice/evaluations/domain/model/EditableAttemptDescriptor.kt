package com.gdavidpb.tuindice.evaluations.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.GradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting

data class EditableAttemptDescriptor(
	val id: String,
	val termId: String,
	val code: String,
	val name: String,
	val credits: Int,
	val grade: Int,
	val gradingMode: GradingMode = GradingMode.NUMERIC,
	// The attempt's valid weekly meetings: what an evaluation of it inherits as its place. Empty when
	// the term has no schedule for it.
	val meetings: List<ScheduleMeeting> = emptyList()
)
