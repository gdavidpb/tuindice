package com.gdavidpb.tuindice.record.domain.usecase.param

import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode

data class UpsertAttemptSelectionParams(
	val viewMode: RecordViewMode,
	val termId: String,
	val attemptId: String,
	val grade: Int?,
	val status: SubjectStatus?,
	val commit: Boolean
)
