package com.gdavidpb.tuindice.record.domain.service

import com.gdavidpb.tuindice.base.domain.model.subject.GradingMode
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import com.gdavidpb.tuindice.record.data.model.quarter.LocalSubject

private const val MIN_APPROVED_GRADE = 3

internal fun LocalSubject.resolvedOutcome(): SubjectStatus {
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

internal fun LocalSubject.countsTowardNumericAverage(): Boolean {
	return gradingMode == GradingMode.NUMERIC &&
		resolvedOutcome() !in setOf(
			SubjectStatus.NORMAL,
			SubjectStatus.RETIRED
		) &&
		grade > 0
}

internal fun LocalSubject.numericCreditsContribution(): Int =
	if (countsTowardNumericAverage()) credits else 0

internal fun LocalSubject.numericWeightedContribution(): Long =
	if (countsTowardNumericAverage()) grade.toLong() * credits.toLong() else 0L

internal fun LocalSubject.isApprovalEvent(): Boolean =
	resolvedOutcome() == SubjectStatus.APPROVED

internal fun LocalSubject.isResolvedQualitativeOutcome(): Boolean {
	return gradingMode == GradingMode.QUALITATIVE_PASS_FAIL &&
		resolvedOutcome() in setOf(SubjectStatus.APPROVED, SubjectStatus.FAILED)
}

internal fun LocalSubject.countsTowardRetakeTimeline(): Boolean =
	countsTowardNumericAverage() || isResolvedQualitativeOutcome()
