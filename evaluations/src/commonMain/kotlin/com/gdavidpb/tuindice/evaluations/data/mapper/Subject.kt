package com.gdavidpb.tuindice.evaluations.data.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.base.domain.model.subject.GradingMode
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.evaluations.data.model.LocalSubject
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicAttemptProjectionEntity

fun AcademicAttemptProjectionEntity.toLocalSubject() = LocalSubject(
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

fun LocalSubject.toSubject() = Subject(
	id = id,
	termId = termId,
	code = code,
	name = name,
	credits = credits,
	grade = grade,
	gradingMode = gradingMode
)
