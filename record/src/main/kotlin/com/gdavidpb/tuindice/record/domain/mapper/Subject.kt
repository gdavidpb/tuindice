package com.gdavidpb.tuindice.record.domain.mapper

import com.gdavidpb.tuindice.record.domain.model.UpdateSubject
import com.gdavidpb.tuindice.record.domain.param.UpdateSubjectParams

fun UpdateSubjectParams.toUpdateSubject() = UpdateSubject(
	subjectId = subjectId,
	grade = grade
)