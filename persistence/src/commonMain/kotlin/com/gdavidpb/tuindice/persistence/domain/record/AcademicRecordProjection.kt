package com.gdavidpb.tuindice.persistence.domain.record

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptBadge
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOverride
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope

typealias AcademicRecordMutationEnvelope = MutationEnvelope<String, AcademicRecordMutation>

fun List<AcademicRecordMutationEnvelope>.sortedForReplay(): List<AcademicRecordMutationEnvelope> =
	sortedWith(
		compareBy(
			AcademicRecordMutationEnvelope::createdAt,
			AcademicRecordMutationEnvelope::mutationId
		)
	)

fun AcademicRecord.reapplying(
	pendingMutations: List<AcademicRecordMutationEnvelope>
): AcademicRecord {
	return pendingMutations.fold(this) { currentRecord, pendingMutation ->
		currentRecord.reapplying(
			mutation = pendingMutation.command,
			updatedAtMillis = pendingMutation.updatedAt
		)
	}
}

private fun AcademicRecord.reapplying(
	mutation: AcademicRecordMutation,
	updatedAtMillis: Long
): AcademicRecord {
	return when (mutation) {
		is AcademicRecordMutation.UpsertAttemptOverride ->
			copy(
				attemptOverrides = attemptOverrides
					.filterNot { override -> override.attemptId == mutation.attemptId } +
					AttemptOverride(
						attemptId = mutation.attemptId,
						score = mutation.score,
						outcome = mutation.outcome,
						updatedAtMillis = updatedAtMillis
					)
			)

		is AcademicRecordMutation.DeleteAttemptOverride ->
			copy(
				attemptOverrides = attemptOverrides.filterNot { override ->
					override.attemptId == mutation.attemptId
				}
			)

		is AcademicRecordMutation.AddSyntheticTerm ->
			copy(
				terms = normalizeTerms(
					terms.filterNot { term -> term.id == mutation.termId } + mutation.toAcademicTerm()
				)
			)

		is AcademicRecordMutation.UpdateSyntheticTerm -> reapplying(mutation)

		is AcademicRecordMutation.DeleteSyntheticTerm -> {
			val removedAttemptIds = terms.firstOrNull { term -> term.id == mutation.termId }
				?.attempts
				?.map(AcademicAttempt::id)
				?.toSet()
				.orEmpty()
			copy(
				terms = terms.filterNot { term -> term.id == mutation.termId },
				attemptOverrides = attemptOverrides.filterNot { override ->
					override.attemptId in removedAttemptIds
				}
			)
		}
	}
}

private fun AcademicRecord.reapplying(
	mutation: AcademicRecordMutation.UpdateSyntheticTerm
): AcademicRecord {
	val updatedTerms = normalizeTerms(
		terms.filterNot { term ->
			term.id == mutation.targetTermId || term.termKey == mutation.targetTermKey
		} + mutation.toAcademicTerm()
	)
	val availableAttemptIds = updatedTerms
		.flatMap(AcademicTerm::attempts)
		.map(AcademicAttempt::id)
		.toSet()

	return copy(
		terms = updatedTerms,
		attemptOverrides = attemptOverrides.filter { override ->
			override.attemptId in availableAttemptIds
		}
	)
}

private fun normalizeTerms(terms: List<AcademicTerm>): List<AcademicTerm> {
	return terms.sortedWith(
		compareBy(AcademicTerm::termOrder, AcademicTerm::id)
	)
}

private fun AcademicRecordMutation.AddSyntheticTerm.toAcademicTerm(): AcademicTerm {
	return AcademicTerm(
		id = termId,
		periodYear = periodYear,
		periodCode = periodCode,
		kind = TermKind.SYNTHETIC,
		attempts = attempts.map { attempt ->
			AcademicAttempt(
				id = attempt.attemptId,
				subjectCode = attempt.subjectCode,
				subjectName = attempt.subjectName,
				credits = attempt.credits,
				gradingMode = attempt.gradingMode,
				academicScore = attempt.score ?: AttemptScore.empty(),
				academicOutcome = attempt.outcome ?: AttemptOutcome.PENDING,
				academicBadge = AttemptBadge.NONE
			)
		}
	)
}

private fun AcademicRecordMutation.UpdateSyntheticTerm.toAcademicTerm(): AcademicTerm {
	return AcademicTerm(
		id = termId,
		periodYear = periodYear,
		periodCode = periodCode,
		kind = TermKind.SYNTHETIC,
		attempts = attempts.map { attempt ->
			AcademicAttempt(
				id = attempt.attemptId,
				subjectCode = attempt.subjectCode,
				subjectName = attempt.subjectName,
				credits = attempt.credits,
				gradingMode = attempt.gradingMode,
				academicScore = attempt.score ?: AttemptScore.empty(),
				academicOutcome = attempt.outcome ?: AttemptOutcome.PENDING,
				academicBadge = AttemptBadge.NONE
			)
		}
	)
}
