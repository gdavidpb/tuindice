package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicScheduleEntry
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection
import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting

/**
 * The valid meetings of the attempt: its placeable schedule entries, with the same meeting listed
 * twice counted once. A withdrawn attempt has none.
 */
fun AcademicAttempt.validMeetings(): List<ScheduleMeeting> {
	return toMeetings(
		attemptId = id,
		subjectCode = subjectCode,
		section = section,
		withdrawn = withdrawn,
		schedule = schedule
	)
}

/** The valid meetings of the projected attempt; see [AcademicAttempt.validMeetings]. */
fun AttemptProjection.validMeetings(): List<ScheduleMeeting> {
	return toMeetings(
		attemptId = id,
		subjectCode = subjectCode,
		section = section,
		withdrawn = withdrawn,
		schedule = schedule
	)
}

private fun toMeetings(
	attemptId: String,
	subjectCode: String,
	section: Int?,
	withdrawn: Boolean,
	schedule: List<AcademicScheduleEntry>?
): List<ScheduleMeeting> {
	if (withdrawn) return emptyList()

	return schedule.orEmpty()
		.filter(AcademicScheduleEngine::isPlaceable)
		.map { entry ->
			ScheduleMeeting(
				attemptId = attemptId,
				subjectCode = subjectCode,
				section = section,
				dayOfWeek = entry.dayOfWeek,
				startBlock = entry.startBlock,
				endBlock = entry.endBlock,
				classroom = entry.classroom.trim()
			)
		}
		.distinct()
}
