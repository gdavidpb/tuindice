package com.gdavidpb.tuindice.record.domain.mapper

import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.record.domain.model.SubjectUpdate

fun Subject.toSubjectUpdate() = SubjectUpdate(
	id = id,
	grade = grade
)