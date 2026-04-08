package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.HistoricalBadge
import com.gdavidpb.tuindice.academiccore.domain.model.OfficialOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.ProjectionSummary
import com.gdavidpb.tuindice.academiccore.domain.model.RecordProjection

object AcademicSummaryEngine {
	fun summarize(projection: RecordProjection): ProjectionSummary {
		val attempts = projection.terms.flatMap { term -> term.attempts }
		val latestGrade = projection.terms.firstOrNull()?.gradeSum ?: 0.0
		val enrolledCredits = attempts.sumOf { attempt -> attempt.credits }
		val approvedCredits = attempts
			.filter { attempt -> attempt.outcome == OfficialOutcome.APPROVED }
			.sumOf { attempt -> attempt.credits }
		val retiredCredits = attempts
			.filter { attempt -> attempt.outcome == OfficialOutcome.RETIRED }
			.sumOf { attempt -> attempt.credits }
		val failedCredits = attempts
			.filter { attempt -> attempt.outcome == OfficialOutcome.FAILED }
			.sumOf { attempt -> attempt.credits }
		val denominator = enrolledCredits.toDouble().takeIf { it > 0.0 } ?: 1.0

		return ProjectionSummary(
			grade = latestGrade,
			enrolledSubjects = attempts.size,
			enrolledCredits = enrolledCredits,
			approvedSubjects = attempts.count { attempt -> attempt.outcome == OfficialOutcome.APPROVED },
			approvedCredits = approvedCredits,
			approvedRelation = if (enrolledCredits == 0) 0.0 else approvedCredits / denominator,
			retiredSubjects = attempts.count { attempt -> attempt.outcome == OfficialOutcome.RETIRED },
			retiredCredits = retiredCredits,
			retiredRelation = if (enrolledCredits == 0) 0.0 else retiredCredits / denominator,
			failedSubjects = attempts.count { attempt -> attempt.outcome == OfficialOutcome.FAILED },
			failedCredits = failedCredits,
			failedRelation = if (enrolledCredits == 0) 0.0 else failedCredits / denominator,
			withoutEffectAttempts = attempts.count { attempt -> attempt.badge == HistoricalBadge.WITHOUT_EFFECT }
		)
	}
}
