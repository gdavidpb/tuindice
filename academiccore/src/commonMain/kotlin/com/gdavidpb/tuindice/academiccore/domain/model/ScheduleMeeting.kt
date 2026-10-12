package com.gdavidpb.tuindice.academiccore.domain.model

/**
 * One valid weekly meeting of an attempt. [dayOfWeek] follows the model's convention
 * (1 = Sunday ... 7 = Saturday); [startBlock] and [endBlock] are the university's class blocks,
 * both inclusive. [classroom] is trimmed and empty when none was assigned.
 */
data class ScheduleMeeting(
	val attemptId: String,
	val subjectCode: String,
	val section: Int?,
	val dayOfWeek: Int,
	val startBlock: Int,
	val endBlock: Int,
	val classroom: String
)
