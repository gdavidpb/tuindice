package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcome
import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcomeKind
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationPrecondition
import com.gdavidpb.tuindice.persistence.domain.mutation.StoreBackedMutationEngine
import com.gdavidpb.tuindice.persistence.domain.record.AcademicRecordMutation
import com.gdavidpb.tuindice.persistence.domain.record.RECORD_MUTATION_SCOPE
import com.gdavidpb.tuindice.record.data.model.VersionedAcademicRecord
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
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

	// Each edit the reconciliation drops is settled at once, as cancelled, so whoever follows it stops waiting.
	// An edit that was already a terminal rejection keeps the result it was published with: nothing more is published.
	@Test
	fun reconcile_publishesCancelledAtOnceForEachEditItDrops_andNothingMoreForARejectedOne() = runTest {
		val store = InMemoryMutationEnvelopeStore(
			listOf(
				gradeEdit("kept", attemptId = "attempt-1"),
				gradeEdit("orphan", attemptId = "attempt-gone"),
				gradeEdit("orphan-terminal", attemptId = "attempt-gone", status = PendingMutationStatus.FailedTerminal),
				recordMutationEnvelope(
					"orphan-delete",
					AcademicRecordMutation.DeleteAttemptOverride("attempt-gone")
				)
			)
		)
		val engine = createMutationEngine(this, outboxStore = store)
		val outcomes = collectOutcomes(engine)
		val dataSource = AcademicRecordOutboxDataSource(engine)

		dataSource.reconcileWithConfirmedRecord(recordWithAttempts("attempt-1", revision = 5L))

		assertEquals(
			listOf(
				MutationOutcome("orphan", MutationOutcomeKind.CANCELLED),
				MutationOutcome("orphan-delete", MutationOutcomeKind.CANCELLED)
			),
			outcomes
		)
	}

	// The record predates the revision this edit expects, so the attempt the edit aims at may have come with that newer
	// revision and not be in the record yet: its absence says nothing. The edit is kept, and nothing is published for it.
	@Test
	fun reconcile_keepsAnEditAimedAtARevisionAheadOfTheRecord_andPublishesNothingForIt() = runTest {
		val store = InMemoryMutationEnvelopeStore(
			listOf(gradeEdit("ahead", attemptId = "attempt-new", revision = 9L))
		)
		val engine = createMutationEngine(this, outboxStore = store)
		val outcomes = collectOutcomes(engine)
		val dataSource = AcademicRecordOutboxDataSource(engine)

		dataSource.reconcileWithConfirmedRecord(recordWithAttempts("attempt-1", revision = 5L))

		assertEquals(listOf("ahead"), store.getMutations(RECORD_MUTATION_SCOPE).map { it.mutationId })
		assertEquals(emptyList(), outcomes)
	}

	// A Failed edit waits for a newer record just as a Pending one does, so the attempt it aims at may simply
	// not be in this record yet.
	@Test
	fun reconcile_keepsAFailedEditAimedAtARevisionAheadOfTheRecord_andPublishesNothingForIt() = runTest {
		val store = InMemoryMutationEnvelopeStore(
			listOf(
				gradeEdit(
					id = "failed-ahead",
					attemptId = "attempt-new",
					status = PendingMutationStatus.Failed,
					revision = 9L
				)
			)
		)
		val engine = createMutationEngine(this, outboxStore = store)
		val outcomes = collectOutcomes(engine)
		val dataSource = AcademicRecordOutboxDataSource(engine)

		dataSource.reconcileWithConfirmedRecord(recordWithAttempts("attempt-1", revision = 5L))

		assertEquals(listOf("failed-ahead"), store.getMutations(RECORD_MUTATION_SCOPE).map { it.mutationId })
		assertEquals(emptyList(), outcomes)
	}

	// A FailedTerminal edit is not waiting for a record: the server rejected it for good, so this record judges it
	// whatever revision it expects. Its rejection was published when it happened, so nothing more is published now.
	@Test
	fun reconcile_dropsARejectedEditAimedAtARevisionAheadOfTheRecord_andPublishesNothingForIt() = runTest {
		val store = InMemoryMutationEnvelopeStore(
			listOf(
				gradeEdit(
					id = "rejected",
					attemptId = "attempt-gone",
					status = PendingMutationStatus.FailedTerminal,
					revision = 9L
				)
			)
		)
		val engine = createMutationEngine(this, outboxStore = store)
		val outcomes = collectOutcomes(engine)
		val dataSource = AcademicRecordOutboxDataSource(engine)

		dataSource.reconcileWithConfirmedRecord(recordWithAttempts("attempt-1", revision = 5L))

		assertEquals(emptyList(), store.getMutations(RECORD_MUTATION_SCOPE))
		assertEquals(emptyList(), outcomes)
	}

	// An edit expecting the record's revision, or an older one, is judged by the record: its absent attempt is dropped.
	@Test
	fun reconcile_dropsAnEditAimedAtTheRevisionOfTheRecord() = runTest {
		val store = InMemoryMutationEnvelopeStore(
			listOf(gradeEdit("same", attemptId = "attempt-gone", revision = 5L))
		)
		val engine = createMutationEngine(this, outboxStore = store)
		val outcomes = collectOutcomes(engine)
		val dataSource = AcademicRecordOutboxDataSource(engine)

		dataSource.reconcileWithConfirmedRecord(recordWithAttempts("attempt-1", revision = 5L))

		assertEquals(emptyList(), store.getMutations(RECORD_MUTATION_SCOPE))
		assertEquals(listOf(MutationOutcome("same", MutationOutcomeKind.CANCELLED)), outcomes)
	}

	// An edit with no precondition expects no revision, so nothing holds it back: an attempt the record lacks is
	// enough to drop it, and its cancellation is published at once.
	@Test
	fun reconcile_dropsAnEditWithNoExpectedRevision_andPublishesCancelledForIt() = runTest {
		val unpreconditioned = gradeEdit("no-revision", attemptId = "attempt-gone")
			.copy(precondition = MutationPrecondition.None)
		val store = InMemoryMutationEnvelopeStore(listOf(unpreconditioned))
		val engine = createMutationEngine(this, outboxStore = store)
		val outcomes = collectOutcomes(engine)
		val dataSource = AcademicRecordOutboxDataSource(engine)

		dataSource.reconcileWithConfirmedRecord(recordWithAttempts("attempt-1", revision = 5L))

		assertEquals(emptyList(), store.getMutations(RECORD_MUTATION_SCOPE))
		assertEquals(listOf(MutationOutcome("no-revision", MutationOutcomeKind.CANCELLED)), outcomes)
	}

	private fun TestScope.collectOutcomes(
		engine: StoreBackedMutationEngine<String, AcademicRecordMutation, VersionedAcademicRecord>
	): MutableList<MutationOutcome> {
		val collected = mutableListOf<MutationOutcome>()

		backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
			engine.outcomes.collect { outcome -> collected += outcome }
		}

		return collected
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
