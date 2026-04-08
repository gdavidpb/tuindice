package com.gdavidpb.tuindice.record.domain.model

import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus

data class SubjectGradeSet(
	val id: String,
	val quarterId: String,
	val grade: Int? = null,
	val status: SubjectStatus? = null,
	val commit: Boolean
)
