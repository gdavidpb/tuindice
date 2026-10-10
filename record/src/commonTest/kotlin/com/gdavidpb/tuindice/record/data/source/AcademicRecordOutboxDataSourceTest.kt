package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationPrecondition
import com.gdavidpb.tuindice.persistence.domain.record.AcademicRecordMutation
import com.gdavidpb.tuindice.persistence.domain.record.RECORD_MUTATION_SCOPE
import com.gdavidpb.tuindice.record.data.model.VersionedAcademicRecord
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AcademicRecordOutboxDataSourceTest {
	@Test
	fun reconcile_dropsEditsAimedAtASubjectTheRecordNoLongerHas() = runTest {
		val store = InMemoryMutationEnvelopeStore(
			listOf(
				gradeEdit("kept", attemptId = "attempt-1"),
				gradeEdit("orphan", attemptId = "attempt-gone"),
				gradeEdit("orphan-terminal", attemptId = "attempt-gone", status = PendingMutationStatus.FailedTerminal),
				recordMutationEnvelope(
					"orphan-delete",
					AcademicRecordMutation.DeleteAttemptOverride("attempt-gone")
				),
				recordMutationEnvelope("term-delete", AcademicRecordMutation.DeleteSyntheticTerm("term-9"))
			)
		)
		val dataSource = AcademicRecordOutboxDataSource(createMutationEngine(this, outboxStore = store))

		dataSource.reconcileWithConfirmedRecord(recordWithAttempts("attempt-1", revision = 5L))

		assertEquals(
			listOf("kept", "term-delete"),
			store.getMutations(RECORD_MUTATION_SCOPE).map { it.mutationId }
		)
	}

	@Test
	fun reconcile_keepsEditsAimedAtASubjectOfATermThatIsStillWaitingToBeSent() = runTest {
		val store = InMemoryMutationEnvelopeStore(
			listOf(
				recordMutationEnvelope(
					"add-term",
					AcademicRecordMutation.AddSyntheticTerm(
						termId = "term-new",
						periodYear = 2026,
						periodCode = AcademicTermPeriod.JUL_AUG,
						attempts = listOf(seed("attempt-new"))
					)
				),
				gradeEdit("grade-on-new", attemptId = "attempt-new")
			)
		)
		val dataSource = AcademicRecordOutboxDataSource(createMutationEngine(this, outboxStore = store))

		dataSource.reconcileWithConfirmedRecord(recordWithAttempts("attempt-1", revision = 5L))

		assertEquals(
			listOf("add-term", "grade-on-new"),
			store.getMutations(RECORD_MUTATION_SCOPE).map { it.mutationId }
		)
	}

	@Test
	fun reconcile_movesTheEditsStillWaitingToTheRevisionOfTheRecord() = runTest {
		val store = InMemoryMutationEnvelopeStore(
			listOf(
				gradeEdit("old", attemptId = "attempt-1", revision = 2L),
				gradeEdit("ahead", attemptId = "attempt-1b", revision = 8L),
				gradeEdit("terminal", attemptId = "attempt-1", status = PendingMutationStatus.FailedTerminal, revision = 2L)
			)
		)
		val dataSource = AcademicRecordOutboxDataSource(createMutationEngine(this, outboxStore = store))

		dataSource.reconcileWithConfirmedRecord(recordWithAttempts("attempt-1", "attempt-1b", revision = 5L))

		val revisions = store.getMutations(RECORD_MUTATION_SCOPE).associate { it.mutationId to it.expectedRevision }

		assertEquals(5L, revisions["old"])
		assertEquals(8L, revisions["ahead"])
		assertEquals(2L, revisions["terminal"])
	}

	private fun gradeEdit(
		id: String,
		attemptId: String,
		status: PendingMutationStatus = PendingMutationStatus.Pending,
		revision: Long = 1L
	) = recordMutationEnvelope(
		id = id,
		command = AcademicRecordMutation.UpsertAttemptOverride(
			attemptId = attemptId,
			score = AttemptScore.numeric(4),
			outcome = null
		),
		status = status,
		revision = revision
	)

	private fun recordMutationEnvelope(
		id: String,
		command: AcademicRecordMutation,
		status: PendingMutationStatus = PendingMutationStatus.Pending,
		revision: Long = 1L
	): MutationEnvelope<String, AcademicRecordMutation> = MutationEnvelope(
		mutationId = id,
		scopeKey = RECORD_MUTATION_SCOPE,
		command = command,
		precondition = MutationPrecondition.Revision(revision),
		status = status,
		createdAt = 1L,
		updatedAt = 1L,
		lastError = null
	)

	private fun seed(attemptId: String) = AcademicRecordMutation.AddSyntheticTerm.SyntheticAttemptSeed(
		attemptId = attemptId,
		subjectCode = "MA1111",
		subjectName = "Matematicas 1",
		credits = 4,
		gradingMode = AttemptGradingMode.NUMERIC
	)

	private fun recordWithAttempts(vararg attemptIds: String, revision: Long) = VersionedAcademicRecord(
		revision = revision,
		record = AcademicRecord(
			id = "record-1",
			terms = listOf(
				AcademicTerm(
					id = "term-1",
					periodYear = 2026,
					periodCode = AcademicTermPeriod.JAN_MAR,
					kind = TermKind.CURRENT,
					attempts = attemptIds.map { id ->
						AcademicAttempt(id = id, subjectCode = "MA1111", subjectName = "Matematicas 1", credits = 4)
					}
				)
			)
		)
	)
}
