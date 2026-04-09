package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.*
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicAttemptEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicAttemptOverrideEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicRecordEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicTermEntity
import com.gdavidpb.tuindice.record.data.mutation.AcademicRecordMutation

internal fun List<AcademicTermEntity>.toAcademicTerms(
	attempts: List<AcademicAttemptEntity>
): List<AcademicTerm> {
	val attemptsByTermId = attempts.groupBy(AcademicAttemptEntity::termId)
	return sortedWith(
		compareBy(AcademicTermEntity::startAt, AcademicTermEntity::endAt, AcademicTermEntity::id)
	).map { term ->
		AcademicTerm(
			id = term.id,
			label = term.label,
			startAtMillis = term.startAt,
			endAtMillis = term.endAt,
			kind = TermKind.valueOf(term.kind),
			attempts = attemptsByTermId[term.id].orEmpty()
				.sortedWith(
					compareBy(AcademicAttemptEntity::positionInTerm, AcademicAttemptEntity::id)
				)
				.map(AcademicAttemptEntity::toAcademicAttempt)
		)
	}
}

internal fun AcademicRecordEntity.revisionValue(): Long = revision

internal fun AcademicTerm.toAcademicTermEntity(): AcademicTermEntity {
	return AcademicTermEntity(
		id = id,
		label = label,
		startAt = startAtMillis,
		endAt = endAtMillis,
		kind = kind.name
	)
}

internal fun AcademicAttempt.toAcademicAttemptEntity(
	termId: String,
	positionInTerm: Int
): AcademicAttemptEntity {
	return AcademicAttemptEntity(
		id = id,
		termId = termId,
		subjectCode = subjectCode,
		subjectName = subjectName,
		credits = credits,
		positionInTerm = positionInTerm,
		gradingMode = gradingMode.name,
		scoreKind = officialScore.storageType,
		scoreNumericValue = officialScore.numericValue,
		scoreSymbolicValue = officialScore.symbolicValue,
		officialOutcome = officialOutcome.name,
		officialBadge = officialBadge.name
	)
}

internal fun AttemptOverride.toAcademicAttemptOverrideEntity(): AcademicAttemptOverrideEntity {
	return AcademicAttemptOverrideEntity(
		attemptId = attemptId,
		scoreKind = score?.storageType,
		scoreNumericValue = score?.numericValue,
		scoreSymbolicValue = score?.symbolicValue,
		outcome = outcome?.name,
		updatedAt = updatedAtMillis
	)
}

internal fun AcademicAttemptEntity.toAcademicAttempt(): AcademicAttempt {
	return AcademicAttempt(
		id = id,
		subjectCode = subjectCode,
		subjectName = subjectName,
		credits = credits,
		gradingMode = AttemptGradingMode.valueOf(gradingMode),
		officialScore = scoreFromStorage(
			type = scoreKind,
			numericValue = scoreNumericValue,
			symbolicValue = scoreSymbolicValue
		),
		officialOutcome = AttemptOutcome.valueOf(officialOutcome),
		officialBadge = AttemptBadge.valueOf(officialBadge)
	)
}

internal fun AcademicAttemptOverrideEntity.toAttemptOverride(): AttemptOverride {
	return AttemptOverride(
		attemptId = attemptId,
		score = scoreKind?.let { kind ->
			scoreFromStorage(
				type = kind,
				numericValue = scoreNumericValue,
				symbolicValue = scoreSymbolicValue
			)
		},
		outcome = outcome?.let(AttemptOutcome::valueOf),
		updatedAtMillis = updatedAt
	)
}

internal fun AcademicRecordMutation.AddSyntheticTerm.toAcademicTerm(): AcademicTerm {
	return AcademicTerm(
		id = termId,
		label = label,
		startAtMillis = startAtMillis,
		endAtMillis = endAtMillis,
		kind = TermKind.SYNTHETIC,
		attempts = attempts.map { attempt ->
			AcademicAttempt(
				id = attempt.attemptId,
				subjectCode = attempt.subjectCode,
				subjectName = attempt.subjectName,
				credits = attempt.credits,
				gradingMode = attempt.gradingMode,
				officialScore = attempt.score ?: AttemptScore.empty(),
				officialOutcome = attempt.outcome ?: AttemptOutcome.PENDING,
				officialBadge = AttemptBadge.NONE
			)
		}
	)
}

private val AttemptScore.storageType: String
	get() = when (this) {
		AttemptScore.Empty -> "EMPTY"
		is AttemptScore.Numeric -> "NUMERIC"
		is AttemptScore.Symbolic -> "SYMBOLIC"
	}

private fun scoreFromStorage(
	type: String,
	numericValue: Int?,
	symbolicValue: String?
): AttemptScore {
	return when (type) {
		"NUMERIC" -> AttemptScore.numeric(numericValue ?: 0)
		"SYMBOLIC" -> AttemptScore.symbolic(symbolicValue ?: "")
		else -> AttemptScore.empty()
	}
}
