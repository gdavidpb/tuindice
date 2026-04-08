package com.gdavidpb.tuindice.record.domain.usecase.param

import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus

data class SetSubjectGradeParams(
	val quarterId: String,
	val subjectId: String,
	val grade: Int? = null,
	val status: SubjectStatus? = null,
	val commit: Boolean
)
