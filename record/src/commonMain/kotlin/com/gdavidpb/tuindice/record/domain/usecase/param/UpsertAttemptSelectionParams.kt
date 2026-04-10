package com.gdavidpb.tuindice.record.domain.usecase.param

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode

data class UpsertAttemptSelectionParams(
	val viewMode: RecordViewMode,
	val termId: String,
	val attemptId: String,
	val grade: Int?,
	val outcome: AttemptOutcome?,
	val commit: Boolean
)
