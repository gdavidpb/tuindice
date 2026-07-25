package com.gdavidpb.tuindice.persistence.domain.record

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOverride
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationPrecondition
import kotlin.test.Test
import kotlin.test.assertEquals

class AcademicRecordProjectionTest {
	@Test
	fun reapplying_pendingAddSyntheticTerm_projectsTermSortedByTermOrder() {
		val record = confirmedRecord()
		val mutation = envelope(
			mutationId = "mutation-1",
			command = addSyntheticTerm(termId = "term-synthetic")
		)

		val visibleRecord = record.reapplying(listOf(mutation))

		assertEquals(
			listOf("term-confirmed" to TermKind.CURRENT, "term-synthetic" to TermKind.SYNTHETIC),
			visibleRecord.terms.map { term -> term.id to term.kind }
		)
	}

	@Test
	fun reapplying_upsertAttemptOverride_takesUpdatedAtFromEnvelope() {
		val mutation = envelope(
			mutationId = "mutation-1",
			command = AcademicRecordMutation.UpsertAttemptOverride(
				attemptId = "attempt-1",
				score = AttemptScore.numeric(5),
				outcome = AttemptOutcome.APPROVED
			),
			createdAt = 10L,
			updatedAt = 42L
		)

		val visibleRecord = confirmedRecord().reapplying(listOf(mutation))

		assertEquals(
			AttemptOverride(
				attemptId = "attempt-1",
				score = AttemptScore.numeric(5),
				outcome = AttemptOutcome.APPROVED,
				updatedAtMillis = 42L
			),
			visibleRecord.attemptOverrides.single()
		)
	}

	@Test
	fun reapplying_deleteSyntheticTerm_dropsTermAndItsOverrides() {
		val added = envelope(
			mutationId = "mutation-1",
			command = addSyntheticTerm(termId = "term-synthetic")
		)
		val overridden = envelope(
			mutationId = "mutation-2",
			command = AcademicRecordMutation.UpsertAttemptOverride(
				attemptId = "attempt-synthetic",
				score = AttemptScore.numeric(4)
			),
			createdAt = 2L
		)
		val deleted = envelope(
			mutationId = "mutation-3",
			command = AcademicRecordMutation.DeleteSyntheticTerm(termId = "term-synthetic"),
			createdAt = 3L
		)

		val visibleRecord = confirmedRecord()
			.reapplying(listOf(added, overridden, deleted).sortedForReplay())

		assertEquals(listOf("term-confirmed"), visibleRecord.terms.map(AcademicTerm::id))
		assertEquals(emptyList(), visibleRecord.attemptOverrides)
	}

	@Test
	fun reapplying_updateSyntheticTerm_replacesTargetTermAndPrunesOrphanOverrides() {
		val added = envelope(
			mutationId = "mutation-1",
			command = addSyntheticTerm(termId = "term-synthetic")
		)
		val overridden = envelope(
			mutationId = "mutation-2",
			command = AcademicRecordMutation.UpsertAttemptOverride(
				attemptId = "attempt-synthetic",
				score = AttemptScore.numeric(4)
			),
			createdAt = 2L
		)
		val updated = envelope(
			mutationId = "mutation-3",
			command = AcademicRecordMutation.UpdateSyntheticTerm(
				targetTermId = "term-synthetic",
				targetTermKey = "2027-JAN_MAR",
				termId = "term-synthetic",
				periodYear = 2027,
				periodCode = AcademicTermPeriod.JAN_MAR,
				attempts = listOf(
					AcademicRecordMutation.UpdateSyntheticTerm.SyntheticAttemptSeed(
						attemptId = "attempt-renamed",
						subjectCode = "MAT2205",
						subjectName = "Ecuaciones Diferenciales",
						credits = 5,
						gradingMode = AttemptGradingMode.NUMERIC
					)
				)
			),
			createdAt = 3L
		)

		val visibleRecord = confirmedRecord()
			.reapplying(listOf(added, overridden, updated).sortedForReplay())

		val syntheticTerm = visibleRecord.terms.single { term -> term.kind == TermKind.SYNTHETIC }
		assertEquals(listOf("attempt-renamed"), syntheticTerm.attempts.map(AcademicAttempt::id))
		assertEquals(emptyList(), visibleRecord.attemptOverrides)
	}

	@Test
	fun sortedForReplay_ordersByCreatedAtThenMutationId() {
		val mutations = listOf(
			envelope("mutation-b", AcademicRecordMutation.DeleteAttemptOverride("attempt-1"), createdAt = 2L),
			envelope("mutation-c", AcademicRecordMutation.DeleteAttemptOverride("attempt-2"), createdAt = 1L),
			envelope("mutation-a", AcademicRecordMutation.DeleteAttemptOverride("attempt-3"), createdAt = 2L)
		)

		assertEquals(
			listOf("mutation-c", "mutation-a", "mutation-b"),
			mutations.sortedForReplay().map(AcademicRecordMutationEnvelope::mutationId)
		)
	}
}

private fun confirmedRecord() = AcademicRecord(
	id = "record-1",
	terms = listOf(
		AcademicTerm(
			id = "term-confirmed",
			periodYear = 2026,
			periodCode = AcademicTermPeriod.JAN_MAR,
			kind = TermKind.CURRENT,
			attempts = listOf(
				AcademicAttempt(
					id = "attempt-1",
					subjectCode = "MAT1203",
					subjectName = "Algebra",
					credits = 4,
					gradingMode = AttemptGradingMode.NUMERIC,
					academicScore = AttemptScore.empty(),
					academicOutcome = AttemptOutcome.PENDING
				)
			)
		)
	)
)

private fun addSyntheticTerm(termId: String) = AcademicRecordMutation.AddSyntheticTerm(
	termId = termId,
	periodYear = 2027,
	periodCode = AcademicTermPeriod.JAN_MAR,
	attempts = listOf(
		AcademicRecordMutation.AddSyntheticTerm.SyntheticAttemptSeed(
			attemptId = "attempt-synthetic",
			subjectCode = "MAT2205",
			subjectName = "Ecuaciones Diferenciales",
			credits = 5,
			gradingMode = AttemptGradingMode.NUMERIC
		)
	)
)

private fun envelope(
	mutationId: String,
	command: AcademicRecordMutation,
	createdAt: Long = 1L,
	updatedAt: Long = createdAt
) = MutationEnvelope(
	mutationId = mutationId,
	scopeKey = RECORD_MUTATION_SCOPE,
	command = command,
	precondition = MutationPrecondition.Revision(1L),
	status = PendingMutationStatus.Pending,
	createdAt = createdAt,
	updatedAt = updatedAt,
	lastError = null
)
