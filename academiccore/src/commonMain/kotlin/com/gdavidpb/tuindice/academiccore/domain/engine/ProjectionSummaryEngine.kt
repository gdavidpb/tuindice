package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptBadge
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.ProjectionSummary
import com.gdavidpb.tuindice.academiccore.domain.model.RecordProjection

object ProjectionSummaryEngine {
	fun summarize(projection: RecordProjection): ProjectionSummary {
		val attempts = projection.terms.flatMap { term -> term.attempts }
		val latestGrade = projection.terms.firstOrNull()?.cumulativeAverage ?: 0.0
		val enrolledCredits = attempts.sumOf { attempt -> attempt.credits }
		val approvedCredits = attempts
			.filter { attempt -> attempt.outcome == AttemptOutcome.APPROVED }
			.sumOf { attempt -> attempt.credits }
		val retiredCredits = attempts
			.filter { attempt -> attempt.outcome == AttemptOutcome.RETIRED }
			.sumOf { attempt -> attempt.credits }
		val failedCredits = attempts
			.filter { attempt -> attempt.outcome == AttemptOutcome.FAILED }
			.sumOf { attempt -> attempt.credits }
		val denominator = enrolledCredits.toDouble().takeIf { value -> value > 0.0 } ?: 1.0

		return ProjectionSummary(
			grade = latestGrade,
			enrolledSubjects = attempts.size,
			enrolledCredits = enrolledCredits,
			approvedSubjects = attempts.count { attempt -> attempt.outcome == AttemptOutcome.APPROVED },
			approvedCredits = approvedCredits,
			approvedRelation = if (enrolledCredits == 0) 0.0 else approvedCredits / denominator,
			retiredSubjects = attempts.count { attempt -> attempt.outcome == AttemptOutcome.RETIRED },
			retiredCredits = retiredCredits,
			retiredRelation = if (enrolledCredits == 0) 0.0 else retiredCredits / denominator,
			failedSubjects = attempts.count { attempt -> attempt.outcome == AttemptOutcome.FAILED },
			failedCredits = failedCredits,
			failedRelation = if (enrolledCredits == 0) 0.0 else failedCredits / denominator,
			withoutEffectAttempts = attempts.count { attempt -> attempt.badge == AttemptBadge.WITHOUT_EFFECT }
		)
	}
}
