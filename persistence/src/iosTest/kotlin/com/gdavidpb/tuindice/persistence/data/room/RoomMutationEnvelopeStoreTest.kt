package com.gdavidpb.tuindice.persistence.data.room

import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.persistence.data.room.daos.createInMemoryTuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.source.RoomPersistenceTransactionRunner
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationPrecondition
import com.gdavidpb.tuindice.persistence.domain.record.AcademicRecordMutation
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RoomMutationEnvelopeStoreTest {
	private lateinit var database: TuIndiceDatabase
	private lateinit var store: RoomMutationEnvelopeStore<AcademicRecordMutation>

	@BeforeTest
	fun setUp() {
		database = createInMemoryTuIndiceDatabase()
		store = store(storeId = "record")
	}

	@AfterTest
	fun tearDown() {
		database.close()
	}

	@Test
	fun advancePendingRevisions_movesOnlyTheWaitingRowsThatExpectLess() = runTest {
		listOf(
			envelope("older", revision = 2L),
			envelope("older-failed", revision = 3L, status = PendingMutationStatus.Failed),
			envelope("ahead", revision = 9L),
			envelope("terminal", revision = 2L, status = PendingMutationStatus.FailedTerminal),
			envelope("no-precondition", precondition = MutationPrecondition.None)
		).forEach { store.savePendingMutation(it) }
		val otherStore = store(storeId = "evaluations")
		otherStore.savePendingMutation(envelope("other-store", revision = 2L))

		val advanced = store.advancePendingRevisions(scopeKey = "record", revision = 5L)

		val byId = store.getMutations("record").associateBy { it.mutationId }
		assertEquals(2, advanced)
		assertEquals(5L, byId.getValue("older").expectedRevision)
		assertEquals(5L, byId.getValue("older-failed").expectedRevision)
		assertEquals(9L, byId.getValue("ahead").expectedRevision)
		assertEquals(2L, byId.getValue("terminal").expectedRevision)
		assertEquals(null, byId.getValue("no-precondition").expectedRevision)
		assertEquals(2L, otherStore.getMutations("record").single().expectedRevision)
	}

	@Test
	fun rebaseCount_survivesTheRoundTripThroughRoom() = runTest {
		store.savePendingMutation(envelope("exhausted").copy(rebaseCount = 4))

		assertEquals(4, store.getMutations("record").single().rebaseCount)
	}

	private fun store(storeId: String) = RoomMutationEnvelopeStore(
		pendingMutationDao = database.pendingMutations,
		transactionRunner = RoomPersistenceTransactionRunner(database),
		storeId = storeId,
		commandSerializer = AcademicRecordMutation.serializer()
	)

	private fun envelope(
		id: String,
		revision: Long = 1L,
		status: PendingMutationStatus = PendingMutationStatus.Pending,
		precondition: MutationPrecondition = MutationPrecondition.Revision(revision)
	): MutationEnvelope<String, AcademicRecordMutation> = MutationEnvelope(
		mutationId = id,
		scopeKey = "record",
		command = AcademicRecordMutation.DeleteAttemptOverride(attemptId = "attempt-$id"),
		precondition = precondition,
		status = status,
		createdAt = 1L,
		updatedAt = 1L,
		lastError = null
	)
}
