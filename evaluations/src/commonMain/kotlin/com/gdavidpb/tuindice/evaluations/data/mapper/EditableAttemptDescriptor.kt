package com.gdavidpb.tuindice.evaluations.data.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.base.domain.model.GradingMode
import com.gdavidpb.tuindice.evaluations.data.model.LocalEditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicAttemptEntity

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
	}
)

fun LocalEditableAttemptDescriptor.toEditableAttemptDescriptor() = EditableAttemptDescriptor(
	id = id,
	termId = termId,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	gradingMode = gradingMode
)
