package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.*
import com.gdavidpb.tuindice.persistence.data.room.entity.*
import com.gdavidpb.tuindice.record.data.mutation.AcademicRecordMutation

internal fun List<AcademicTermEntity>.toAcademicTerms(attempts: List<AcademicAttempt>): List<AcademicTerm> {
	val attemptsByTermId = attempts.groupBy(AcademicAttempt::termId)
	return sortedWith(
		compareByDescending<AcademicTermEntity> { it.startAt }
			.thenBy { it.id }
	).map { term ->
		AcademicTerm(
			id = term.id,
			label = term.label,
			startAtMillis = term.startAt,
			endAtMillis = term.endAt,
			order = term.order,
			kind = TermKind.valueOf(term.kind),
			sourceReportedPeriodAverage = term.sourceReportedPeriodAverage,
			sourceReportedCumulativeAverage = term.sourceReportedCumulativeAverage,
			attempts = attemptsByTermId[term.id].orEmpty()
				.sortedWith(
					compareBy<AcademicAttempt> { it.sequenceInTerm }
						.thenBy { it.id }
				)
		)
	}
}

internal fun List<AcademicLocalTermEntity>.toAcademicLocalTerms(attempts: List<AcademicAttempt>): List<AcademicTerm> {
	val attemptsByTermId = attempts.groupBy(AcademicAttempt::termId)
	return sortedWith(
		compareByDescending<AcademicLocalTermEntity> { it.startAt }
			.thenBy { it.id }
	).map { term ->
		AcademicTerm(
			id = term.id,
			label = term.label,
			startAtMillis = term.startAt,
			endAtMillis = term.endAt,
			order = term.order,
			kind = TermKind.valueOf(term.kind),
			attempts = attemptsByTermId[term.id].orEmpty()
				.sortedWith(
					compareBy<AcademicAttempt> { it.sequenceInTerm }
						.thenBy { it.id }
				)
		)
	}
}

internal fun List<AcademicTermProjectionEntity>.toTermProjections(
	attempts: List<AttemptProjection>
): List<TermProjection> {
	val attemptsByTermId = attempts.groupBy(AttemptProjection::termId)
	return sortedWith(
		compareByDescending<AcademicTermProjectionEntity> { it.startAt }
			.thenBy { it.id }
	).map { term ->
		TermProjection(
			id = term.id,
			label = term.label,
			startAtMillis = term.startAt,
			endAtMillis = term.endAt,
			order = term.order,
			kind = TermKind.valueOf(term.kind),
			grade = term.grade,
			gradeSum = term.gradeSum,
			credits = term.credits,
			creditsSum = term.creditsSum,
			attempts = attemptsByTermId[term.id].orEmpty()
				.sortedWith(
					compareBy<AttemptProjection> { it.sequenceInTerm }
						.thenBy { it.id }
				)
		)
	}
}

internal fun AcademicRecord.toAcademicRecordEntity(): AcademicRecordEntity {
	return AcademicRecordEntity(
		id = id,
		revision = revision,
		updatedAt = updatedAtMillis
	)
}

internal fun AcademicTerm.toAcademicTermEntity(recordId: String): AcademicTermEntity {
	return AcademicTermEntity(
		id = id,
		recordId = recordId,
		label = label,
		startAt = startAtMillis,
		endAt = endAtMillis,
		order = order,
		kind = kind.name,
		sourceReportedPeriodAverage = sourceReportedPeriodAverage,
		sourceReportedCumulativeAverage = sourceReportedCumulativeAverage
	)
}

internal fun AcademicAttempt.toAcademicAttemptEntity(recordId: String): AcademicAttemptEntity {
	return AcademicAttemptEntity(
		id = id,
		recordId = recordId,
		termId = termId,
		subjectCode = subjectCode,
		subjectName = subjectName,
		credits = credits,
		sequenceInTerm = sequenceInTerm,
		gradingMode = gradingMode.name,
		rawGradeToken = rawGradeToken,
		rawObservationText = rawObservationText,
		scoreKind = officialScore.kind.name,
		scoreNumericValue = officialScore.numericValue,
		scoreSymbolicValue = officialScore.symbolicValue,
		officialOutcome = officialOutcome.name,
		officialBadge = officialBadge.name,
		source = source.name
	)
}

internal fun AttemptOverride.toAcademicAttemptOverrideEntity(
	recordId: String,
	committed: Boolean
): AcademicAttemptOverrideEntity {
	return AcademicAttemptOverrideEntity(
		attemptId = attemptId,
		recordId = recordId,
		scoreKind = score?.kind?.name,
		scoreNumericValue = score?.numericValue,
		scoreSymbolicValue = score?.symbolicValue,
		outcome = outcome?.name,
		updatedAt = updatedAtMillis,
		committed = committed
	)
}

internal fun AcademicTerm.toAcademicLocalTermEntity(recordId: String): AcademicLocalTermEntity {
	return AcademicLocalTermEntity(
		id = id,
		recordId = recordId,
		label = label,
		startAt = startAtMillis,
		endAt = endAtMillis,
		order = order,
		kind = kind.name
	)
}

internal fun AcademicAttempt.toAcademicLocalAttemptEntity(recordId: String): AcademicLocalAttemptEntity {
	return AcademicLocalAttemptEntity(
		id = id,
		recordId = recordId,
		termId = termId,
		subjectCode = subjectCode,
		subjectName = subjectName,
		credits = credits,
		sequenceInTerm = sequenceInTerm,
		gradingMode = gradingMode.name,
		rawGradeToken = rawGradeToken,
		rawObservationText = rawObservationText,
		scoreKind = officialScore.kind.name,
		scoreNumericValue = officialScore.numericValue,
		scoreSymbolicValue = officialScore.symbolicValue,
		officialOutcome = officialOutcome.name,
		officialBadge = officialBadge.name,
		source = source.name
	)
}

internal fun TermProjection.toAcademicTermProjectionEntity(
	recordId: String,
	viewMode: ProjectionViewMode
): AcademicTermProjectionEntity {
	return AcademicTermProjectionEntity(
		viewMode = viewMode.storageValue,
		id = id,
		recordId = recordId,
		label = label,
		startAt = startAtMillis,
		endAt = endAtMillis,
		order = order,
		kind = kind.name,
		grade = grade,
		gradeSum = gradeSum,
		credits = credits,
		creditsSum = creditsSum
	)
}

internal fun AttemptProjection.toAcademicAttemptProjectionEntity(
	recordId: String,
	viewMode: ProjectionViewMode
): AcademicAttemptProjectionEntity {
	return AcademicAttemptProjectionEntity(
		viewMode = viewMode.storageValue,
		id = id,
		recordId = recordId,
		termId = termId,
		subjectCode = subjectCode,
		subjectName = subjectName,
		credits = credits,
		sequenceInTerm = sequenceInTerm,
		gradingMode = gradingMode.name,
		rawGradeToken = rawGradeToken,
		rawObservationText = rawObservationText,
		scoreKind = score.kind.name,
		scoreNumericValue = score.numericValue,
		scoreSymbolicValue = score.symbolicValue,
		outcome = outcome.name,
		badge = badge.name,
		countsTowardTermAverage = countsTowardTermAverage,
		countsTowardCumulativeAverage = countsTowardCumulativeAverage
	)
}

internal fun RecordSummary.toAcademicSummaryEntity(recordId: String): AcademicSummaryEntity {
	return AcademicSummaryEntity(
		recordId = recordId,
		officialGrade = official.grade,
		officialEnrolledSubjects = official.enrolledSubjects,
		officialEnrolledCredits = official.enrolledCredits,
		officialApprovedSubjects = official.approvedSubjects,
		officialApprovedCredits = official.approvedCredits,
		officialApprovedRelation = official.approvedRelation,
		officialRetiredSubjects = official.retiredSubjects,
		officialRetiredCredits = official.retiredCredits,
		officialRetiredRelation = official.retiredRelation,
		officialFailedSubjects = official.failedSubjects,
		officialFailedCredits = official.failedCredits,
		officialFailedRelation = official.failedRelation,
		officialWithoutEffectAttempts = official.withoutEffectAttempts,
		simulationGrade = simulation.grade,
		simulationEnrolledSubjects = simulation.enrolledSubjects,
		simulationEnrolledCredits = simulation.enrolledCredits,
		simulationApprovedSubjects = simulation.approvedSubjects,
		simulationApprovedCredits = simulation.approvedCredits,
		simulationApprovedRelation = simulation.approvedRelation,
		simulationRetiredSubjects = simulation.retiredSubjects,
		simulationRetiredCredits = simulation.retiredCredits,
		simulationRetiredRelation = simulation.retiredRelation,
		simulationFailedSubjects = simulation.failedSubjects,
		simulationFailedCredits = simulation.failedCredits,
		simulationFailedRelation = simulation.failedRelation,
		simulationWithoutEffectAttempts = simulation.withoutEffectAttempts
	)
}

internal fun AcademicAttemptEntity.toAcademicAttempt(): AcademicAttempt {
	return AcademicAttempt(
		id = id,
		termId = termId,
		subjectCode = subjectCode,
		subjectName = subjectName,
		credits = credits,
		sequenceInTerm = sequenceInTerm,
		gradingMode = AttemptGradingMode.valueOf(gradingMode),
		rawGradeToken = rawGradeToken,
		rawObservationText = rawObservationText,
		officialScore = AttemptScore(
			kind = AttemptScoreKind.valueOf(scoreKind),
			numericValue = scoreNumericValue,
			symbolicValue = scoreSymbolicValue
		),
		officialOutcome = OfficialOutcome.valueOf(officialOutcome),
		officialBadge = HistoricalBadge.valueOf(officialBadge),
		source = AttemptSource.valueOf(source)
	)
}

internal fun AcademicLocalAttemptEntity.toAcademicAttempt(): AcademicAttempt {
	return AcademicAttempt(
		id = id,
		termId = termId,
		subjectCode = subjectCode,
		subjectName = subjectName,
		credits = credits,
		sequenceInTerm = sequenceInTerm,
		gradingMode = AttemptGradingMode.valueOf(gradingMode),
		rawGradeToken = rawGradeToken,
		rawObservationText = rawObservationText,
		officialScore = AttemptScore(
			kind = AttemptScoreKind.valueOf(scoreKind),
			numericValue = scoreNumericValue,
			symbolicValue = scoreSymbolicValue
		),
		officialOutcome = OfficialOutcome.valueOf(officialOutcome),
		officialBadge = HistoricalBadge.valueOf(officialBadge),
		source = AttemptSource.valueOf(source)
	)
}

internal fun AcademicAttemptOverrideEntity.toAttemptOverride(): AttemptOverride {
	return AttemptOverride(
		attemptId = attemptId,
		score = scoreKind?.let { kind ->
			AttemptScore(
				kind = AttemptScoreKind.valueOf(kind),
				numericValue = scoreNumericValue,
				symbolicValue = scoreSymbolicValue
			)
		},
		outcome = outcome?.let(OfficialOutcome::valueOf),
		updatedAtMillis = updatedAt
	)
}

internal fun AcademicAttemptProjectionEntity.toAttemptProjection(): AttemptProjection {
	return AttemptProjection(
		id = id,
		termId = termId,
		subjectCode = subjectCode,
		subjectName = subjectName,
		credits = credits,
		sequenceInTerm = sequenceInTerm,
		gradingMode = AttemptGradingMode.valueOf(gradingMode),
		rawGradeToken = rawGradeToken,
		rawObservationText = rawObservationText,
		score = AttemptScore(
			kind = AttemptScoreKind.valueOf(scoreKind),
			numericValue = scoreNumericValue,
			symbolicValue = scoreSymbolicValue
		),
		outcome = OfficialOutcome.valueOf(outcome),
		badge = HistoricalBadge.valueOf(badge),
		countsTowardTermAverage = countsTowardTermAverage,
		countsTowardCumulativeAverage = countsTowardCumulativeAverage
	)
}

internal fun AcademicSummaryEntity.toRecordSummary(): RecordSummary {
	return RecordSummary(
		official = ProjectionSummary(
			grade = officialGrade,
			enrolledSubjects = officialEnrolledSubjects,
			enrolledCredits = officialEnrolledCredits,
			approvedSubjects = officialApprovedSubjects,
			approvedCredits = officialApprovedCredits,
			approvedRelation = officialApprovedRelation,
			retiredSubjects = officialRetiredSubjects,
			retiredCredits = officialRetiredCredits,
			retiredRelation = officialRetiredRelation,
			failedSubjects = officialFailedSubjects,
			failedCredits = officialFailedCredits,
			failedRelation = officialFailedRelation,
			withoutEffectAttempts = officialWithoutEffectAttempts
		),
		simulation = ProjectionSummary(
			grade = simulationGrade,
			enrolledSubjects = simulationEnrolledSubjects,
			enrolledCredits = simulationEnrolledCredits,
			approvedSubjects = simulationApprovedSubjects,
			approvedCredits = simulationApprovedCredits,
			approvedRelation = simulationApprovedRelation,
			retiredSubjects = simulationRetiredSubjects,
			retiredCredits = simulationRetiredCredits,
			retiredRelation = simulationRetiredRelation,
			failedSubjects = simulationFailedSubjects,
			failedCredits = simulationFailedCredits,
			failedRelation = simulationFailedRelation,
			withoutEffectAttempts = simulationWithoutEffectAttempts
		)
	)
}

internal fun AcademicRecordMutation.AddSyntheticTerm.toAcademicTerm(order: Int): AcademicTerm {
	return AcademicTerm(
		id = termId,
		label = label,
		startAtMillis = startAtMillis,
		endAtMillis = endAtMillis,
		order = order,
		kind = TermKind.SYNTHETIC,
		attempts = attempts.mapIndexed { index, attempt ->
			AcademicAttempt(
				id = attempt.attemptId,
				termId = termId,
				subjectCode = attempt.subjectCode,
				subjectName = attempt.subjectName,
				credits = attempt.credits,
				sequenceInTerm = index,
				gradingMode = attempt.gradingMode,
				officialScore = attempt.score ?: AttemptScore.empty(),
				officialOutcome = attempt.outcome ?: OfficialOutcome.PENDING,
				officialBadge = HistoricalBadge.NONE,
				source = AttemptSource.LOCAL
			)
		}
	)
}

internal val ProjectionViewMode.storageValue: String
	get() = when (this) {
		ProjectionViewMode.OFFICIAL -> "official"
		ProjectionViewMode.SIMULATION -> "simulation"
	}
