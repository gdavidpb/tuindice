package com.gdavidpb.tuindice.evaluations.data.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.validMeetings
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.GradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting
import com.gdavidpb.tuindice.evaluations.data.model.LocalEditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicAttemptEntity
import com.gdavidpb.tuindice.persistence.data.room.mapper.toAcademicAttempt

fun AcademicAttemptEntity.toLocalEditableAttemptDescriptor() = LocalEditableAttemptDescriptor(
	id = id,
	termId = termId,
	code = subjectCode,
	name = subjectName,
	credits = credits,
	grade = scoreNumericValue ?: 0,
	gradingMode = when (AttemptGradingMode.valueOf(gradingMode)) {
		AttemptGradingMode.NUMERIC -> GradingMode.NUMERIC
		AttemptGradingMode.QUALITATIVE_PASS_FAIL -> GradingMode.QUALITATIVE_PASS_FAIL
	},
	meetings = validMeetingsOrEmpty()
)

fun LocalEditableAttemptDescriptor.toEditableAttemptDescriptor() = EditableAttemptDescriptor(
	id = id,
	termId = termId,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	gradingMode = gradingMode,
	meetings = meetings
)

// The schedule is stored as text and read whole. A row that cannot be read has no place to inherit:
// it must not take the list of evaluations down with it.
private fun AcademicAttemptEntity.validMeetingsOrEmpty(): List<ScheduleMeeting> {
	return runCatching { toAcademicAttempt().validMeetings() }.getOrDefault(emptyList())
}
