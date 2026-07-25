package com.gdavidpb.tuindice.record.domain.usecase.param

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome

data class UpsertAttemptSelectionParams(
	val attemptId: String,
	val grade: Int?,
	val outcome: AttemptOutcome?
)
