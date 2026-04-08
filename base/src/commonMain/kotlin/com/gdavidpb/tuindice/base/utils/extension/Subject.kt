package com.gdavidpb.tuindice.base.utils.extension

import com.gdavidpb.tuindice.base.domain.model.subject.GradingMode
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus

private const val MIN_APPROVED_GRADE = 3

fun Subject.resolvedOutcome(): SubjectStatus {
	return when (status ?: SubjectStatus.NORMAL) {
		SubjectStatus.APPROVED,
		SubjectStatus.FAILED,
		SubjectStatus.RETIRED,
		SubjectStatus.WITHOUT_EFFECT -> status ?: SubjectStatus.NORMAL

		SubjectStatus.NORMAL -> when (gradingMode) {
			GradingMode.NUMERIC -> when {
				grade >= MIN_APPROVED_GRADE -> SubjectStatus.APPROVED
				grade > 0 -> SubjectStatus.FAILED
				else -> SubjectStatus.NORMAL
			}

			GradingMode.QUALITATIVE_PASS_FAIL -> SubjectStatus.NORMAL
		}
	}
}

fun Subject.countsTowardNumericAverage(): Boolean {
	return gradingMode == GradingMode.NUMERIC &&
		resolvedOutcome() !in setOf(
			SubjectStatus.NORMAL,
			SubjectStatus.RETIRED,
			SubjectStatus.WITHOUT_EFFECT
		) &&
		grade > 0
}

fun Subject.isApprovalEvent(): Boolean =
	resolvedOutcome() == SubjectStatus.APPROVED

fun Subject.isFailedEvent(): Boolean =
	resolvedOutcome() == SubjectStatus.FAILED
