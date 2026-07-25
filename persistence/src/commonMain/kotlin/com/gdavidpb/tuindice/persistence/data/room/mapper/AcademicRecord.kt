package com.gdavidpb.tuindice.persistence.data.room.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptBadge
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOverride
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicAttemptEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicAttemptOverrideEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicRecordEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicTermEntity

fun List<AcademicTermEntity>.toAcademicTerms(
	attempts: List<AcademicAttemptEntity>
): List<AcademicTerm> {
	val attemptsByTermId = attempts.groupBy(AcademicAttemptEntity::termId)
	return sortedWith(
		compareBy(AcademicTermEntity::termOrder, AcademicTermEntity::id)
	).map { term ->
		AcademicTerm(
			id = term.id,
			periodYear = term.periodYear,
			periodCode = AcademicTermPeriod.valueOf(term.periodCode),
			kind = TermKind.valueOf(term.kind),
			termKey = term.termKey,
			termOrder = term.termOrder,
			periodLabel = term.periodLabel,
			attempts = attemptsByTermId[term.id].orEmpty()
				.sortedWith(
					compareBy(AcademicAttemptEntity::positionInTerm, AcademicAttemptEntity::id)
				)
				.map(AcademicAttemptEntity::toAcademicAttempt)
		)
	}
}

fun AcademicRecordEntity.revisionValue(): Long = revision

fun AcademicTerm.toAcademicTermEntity(): AcademicTermEntity {
	return AcademicTermEntity(
		id = id,
		periodYear = periodYear,
		periodCode = periodCode.name,
		termKey = termKey,
		termOrder = termOrder,
		periodLabel = periodLabel,
		kind = kind.name
	)
}

fun AcademicAttempt.toAcademicAttemptEntity(
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
		scoreKind = academicScore.storageType,
		scoreNumericValue = academicScore.numericValue,
		scoreSymbolicValue = academicScore.symbolicValue,
		academicOutcome = academicOutcome.name,
		academicBadge = academicBadge.name
	)
}

fun AttemptOverride.toAcademicAttemptOverrideEntity(): AcademicAttemptOverrideEntity {
	return AcademicAttemptOverrideEntity(
		attemptId = attemptId,
		scoreKind = score?.storageType,
		scoreNumericValue = score?.numericValue,
		scoreSymbolicValue = score?.symbolicValue,
		outcome = outcome?.name,
		updatedAt = updatedAtMillis
	)
}

fun AcademicAttemptEntity.toAcademicAttempt(): AcademicAttempt {
	return AcademicAttempt(
		id = id,
		subjectCode = subjectCode,
		subjectName = subjectName,
		credits = credits,
		gradingMode = AttemptGradingMode.valueOf(gradingMode),
		academicScore = scoreFromStorage(
			type = scoreKind,
			numericValue = scoreNumericValue,
			symbolicValue = scoreSymbolicValue
		),
		academicOutcome = AttemptOutcome.valueOf(academicOutcome),
		academicBadge = AttemptBadge.valueOf(academicBadge)
	)
}

fun AcademicAttemptOverrideEntity.toAttemptOverride(): AttemptOverride {
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
