package com.gdavidpb.tuindice.base.data.model.database

import com.gdavidpb.tuindice.base.utils.extensions.toSubjectStatus

data class SubjectUpdate(
	val grade: Int,
	val status: Int = grade.toSubjectStatus()
)