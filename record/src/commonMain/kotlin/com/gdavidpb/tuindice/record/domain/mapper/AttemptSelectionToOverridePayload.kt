package com.gdavidpb.tuindice.record.domain.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore

fun attemptSelectionToOverridePayload(
	grade: Int?,
	outcome: AttemptOutcome?
): Pair<AttemptScore?, AttemptOutcome?> {
	val score = grade?.let { AttemptScore.numeric(it) }
	return score to outcome
}
