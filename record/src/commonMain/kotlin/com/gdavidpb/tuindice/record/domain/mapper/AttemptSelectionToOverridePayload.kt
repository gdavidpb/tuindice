package com.gdavidpb.tuindice.record.domain.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus

fun attemptSelectionToOverridePayload(
	grade: Int?,
	status: SubjectStatus?
): Pair<AttemptScore?, AttemptOutcome?> {
	val score = grade?.let { AttemptScore.numeric(it) }
	val outcome = when (status) {
		null -> null
		SubjectStatus.NORMAL -> AttemptOutcome.PENDING
		SubjectStatus.UNREPORTED -> AttemptOutcome.UNREPORTED
		SubjectStatus.APPROVED -> AttemptOutcome.APPROVED
		SubjectStatus.FAILED -> AttemptOutcome.FAILED
		SubjectStatus.RETIRED -> AttemptOutcome.RETIRED
		SubjectStatus.WITHOUT_EFFECT -> null
	}

	return score to outcome
}
