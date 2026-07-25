package com.gdavidpb.tuindice.persistence.data.room.daos

import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.entity.PendingMutationEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PendingMutationDaoTest {
	private lateinit var database: TuIndiceDatabase
	private lateinit var dao: PendingMutationDao

	@BeforeTest
	fun setUp() {
		database = createInMemoryTuIndiceDatabase()
		dao = database.pendingMutations
	}

	@AfterTest
	fun tearDown() {
		database.close()
	}

	@Test
	fun upsertEntity_thenGetPendingMutation_returnsRoundTrippedEntity() = runTest {
		val mutation = pendingMutation(mutationId = "mutation-1")

		dao.upsertEntity(mutation)

		assertEquals(
			mutation,
			dao.getPendingMutation(
				storeId = mutation.storeId,
				scopeKey = mutation.scopeKey,
				mutationId = mutation.mutationId
			)
		)
		assertEquals(
			mutation,
			dao.getPendingMutation(mutationId = mutation.mutationId)
		)
	}

	@Test
	fun getPendingMutation_whenMissing_returnsNull() = runTest {
		assertNull(
			dao.getPendingMutation(
				storeId = "evaluations",
				scopeKey = "scope-1",
				mutationId = "missing"
			)
		)
		assertNull(dao.getPendingMutation(mutationId = "missing"))
	}

	@Test
	fun upsertEntity_withExistingMutationId_replacesRow() = runTest {
		val original = pendingMutation(
			mutationId = "mutation-1",
			status = "Pending",
			updatedAt = 1L
		)
		val updated = original.copy(
			status = "Failed",
			updatedAt = 2L,
			lastError = "boom"
		)

		dao.upsertEntity(original)
		dao.upsertEntity(updated)

		assertEquals(
			updated,
			dao.getPendingMutation(mutationId = original.mutationId)
		)
		assertEquals(
			listOf(updated),
			dao.getMutations(storeId = original.storeId, scopeKey = original.scopeKey)
		)
	}

	@Test
	fun getPendingMutations_returnsOnlyPendingOrderedByCreatedAt() = runTest {
		val newest = pendingMutation(mutationId = "mutation-newest", createdAt = 3L)
		val oldest = pendingMutation(mutationId = "mutation-oldest", createdAt = 1L)
		val failed = pendingMutation(mutationId = "mutation-failed", createdAt = 2L, status = "Failed")
		val otherScope = pendingMutation(mutationId = "mutation-other-scope", scopeKey = "scope-2")

		dao.upsertEntities(listOf(newest, oldest, failed, otherScope))

		assertEquals(
			listOf(oldest, newest),
			dao.getPendingMutations(storeId = "evaluations", scopeKey = "scope-1")
		)
	}

	@Test
	fun getMutations_returnsAllStatusesInScopeOrderedByCreatedAt() = runTest {
		val pending = pendingMutation(mutationId = "mutation-pending", createdAt = 2L)
		val failed = pendingMutation(mutationId = "mutation-failed", createdAt = 1L, status = "Failed")
		val otherStore = pendingMutation(mutationId = "mutation-other-store", storeId = "subjects")

		dao.upsertEntities(listOf(pending, failed, otherStore))

		assertEquals(
			listOf(failed, pending),
			dao.getMutations(storeId = "evaluations", scopeKey = "scope-1")
		)
	}

	@Test
	fun observePendingMutations_emitsOnlyPendingOrderedByCreatedAt() = runTest {
		assertEquals(
			emptyList(),
			dao.observePendingMutations(storeId = "evaluations", scopeKey = "scope-1").first()
		)

		val second = pendingMutation(mutationId = "mutation-2", createdAt = 2L)
		val first = pendingMutation(mutationId = "mutation-1", createdAt = 1L)
		val failed = pendingMutation(mutationId = "mutation-3", createdAt = 3L, status = "Failed")

		dao.upsertEntities(listOf(second, first, failed))

		assertEquals(
			listOf(first, second),
			dao.observePendingMutations(storeId = "evaluations", scopeKey = "scope-1").first()
		)
	}

	@Test
	fun retryFailedMutations_marksFailedAsPendingAndClearsLastError() = runTest {
		val failed = pendingMutation(
			mutationId = "mutation-failed",
			status = "Failed",
			createdAt = 1L,
			updatedAt = 1L,
			lastError = "network error"
		)
		val pending = pendingMutation(mutationId = "mutation-pending", createdAt = 2L)
		val failedInOtherScope = pendingMutation(
			mutationId = "mutation-other-scope",
			scopeKey = "scope-2",
			status = "Failed",
			lastError = "network error"
		)

		dao.upsertEntities(listOf(failed, pending, failedInOtherScope))

		val retried = dao.requeueMutations(
			storeId = "evaluations",
			scopeKey = "scope-1",
			requeueFrom = "Failed",
			retryableBefore = Long.MAX_VALUE,
			updatedAt = 10L
		)

		assertEquals(1, retried)
		assertEquals(
			failed.copy(status = "Pending", updatedAt = 10L, lastError = null),
			dao.getPendingMutation(mutationId = failed.mutationId)
		)
		assertEquals(
			pending,
			dao.getPendingMutation(mutationId = pending.mutationId)
		)
		assertEquals(
			failedInOtherScope,
			dao.getPendingMutation(mutationId = failedInOtherScope.mutationId)
		)
	}

	@Test
	fun deletePendingMutation_removesOnlyMatchingRow() = runTest {
		val target = pendingMutation(mutationId = "mutation-1")
		val other = pendingMutation(mutationId = "mutation-2")

		dao.upsertEntities(listOf(target, other))

		val deleted = dao.deletePendingMutation(
			storeId = target.storeId,
			scopeKey = target.scopeKey,
			mutationId = target.mutationId
		)

		assertEquals(1, deleted)
		assertNull(dao.getPendingMutation(mutationId = target.mutationId))
		assertEquals(
			listOf(other),
			dao.getPendingMutations(storeId = "evaluations", scopeKey = "scope-1")
		)
	}

	@Test
	fun deletePendingMutation_whenMissing_deletesNothing() = runTest {
		val mutation = pendingMutation(mutationId = "mutation-1")

		dao.upsertEntity(mutation)

		val deleted = dao.deletePendingMutation(
			storeId = mutation.storeId,
			scopeKey = "scope-2",
			mutationId = mutation.mutationId
		)

		assertEquals(0, deleted)
		assertEquals(
			mutation,
			dao.getPendingMutation(mutationId = mutation.mutationId)
		)
	}

	@Test
	fun deletePendingMutationsByReplaceKey_removesOnlyMatchingStoreRows() = runTest {
		val replaced = pendingMutation(
			mutationId = "mutation-1",
			replaceKey = "subject:1"
		)
		val sameReplaceKeyOtherStore = pendingMutation(
			mutationId = "mutation-2",
			storeId = "subjects",
			replaceKey = "subject:1"
		)
		val otherReplaceKey = pendingMutation(
			mutationId = "mutation-3",
			replaceKey = "subject:2"
		)

		dao.upsertEntities(listOf(replaced, sameReplaceKeyOtherStore, otherReplaceKey))

		val deleted = dao.deletePendingMutationsByReplaceKey(
			storeId = "evaluations",
			replaceKey = "subject:1"
		)

		assertEquals(1, deleted)
		assertNull(dao.getPendingMutation(mutationId = replaced.mutationId))
		assertEquals(
			sameReplaceKeyOtherStore,
			dao.getPendingMutation(mutationId = sameReplaceKeyOtherStore.mutationId)
		)
		assertEquals(
			otherReplaceKey,
			dao.getPendingMutation(mutationId = otherReplaceKey.mutationId)
		)
	}

	@Test
	fun deleteAll_removesEveryRow() = runTest {
		dao.upsertEntities(
			listOf(
				pendingMutation(mutationId = "mutation-1"),
				pendingMutation(mutationId = "mutation-2", storeId = "subjects"),
				pendingMutation(mutationId = "mutation-3", scopeKey = "scope-2", status = "Failed")
			)
		)

		val deleted = dao.deleteAll()

		assertEquals(3, deleted)
		assertEquals(
			emptyList(),
			dao.getMutations(storeId = "evaluations", scopeKey = "scope-1")
		)
		assertEquals(
			emptyList(),
			dao.getMutations(storeId = "subjects", scopeKey = "scope-1")
		)
	}

	@Test
	fun observeMutations_includesFailedRows_unlikeObservePendingMutations() = runTest {
		dao.upsertEntities(
			listOf(
				pendingMutation(mutationId = "mutation-1", createdAt = 1L),
				pendingMutation(mutationId = "mutation-2", createdAt = 2L, status = "Failed")
			)
		)

		assertEquals(
			listOf("mutation-1"),
			dao.observePendingMutations(storeId = "evaluations", scopeKey = "scope-1")
				.first()
				.map(PendingMutationEntity::mutationId)
		)
		assertEquals(
			listOf("mutation-1"),
			dao.getPendingMutations(storeId = "evaluations", scopeKey = "scope-1")
				.map(PendingMutationEntity::mutationId)
		)

		assertEquals(
			listOf("mutation-1", "mutation-2"),
			dao.observeMutations(storeId = "evaluations", scopeKey = "scope-1")
				.first()
				.map(PendingMutationEntity::mutationId)
		)
		assertEquals(
			listOf("mutation-1", "mutation-2"),
			dao.getMutations(storeId = "evaluations", scopeKey = "scope-1")
				.map(PendingMutationEntity::mutationId)
		)
	}

	@Test
	fun requeueMutations_onlyTouchesMatchingRowsPastTheBackoff() = runTest {
		dao.upsertEntities(
			listOf(
				pendingMutation(mutationId = "stale-failed", status = "Failed", updatedAt = 100L, lastError = "boom"),
				pendingMutation(mutationId = "fresh-failed", status = "Failed", updatedAt = 900L, lastError = "boom"),
				pendingMutation(mutationId = "already-pending", status = "Pending", updatedAt = 100L)
			)
		)

		val requeued = dao.requeueMutations(
			storeId = "evaluations",
			scopeKey = "scope-1",
			requeueFrom = "Failed",
			retryableBefore = 500L,
			updatedAt = 1_000L
		)

		assertEquals(1, requeued)

		val byId = dao.getMutations(storeId = "evaluations", scopeKey = "scope-1")
			.associateBy(PendingMutationEntity::mutationId)

		assertEquals("Pending", byId.getValue("stale-failed").status)
		assertNull(byId.getValue("stale-failed").lastError)
		assertEquals(1_000L, byId.getValue("stale-failed").updatedAt)

		assertEquals("Failed", byId.getValue("fresh-failed").status)
		assertEquals("boom", byId.getValue("fresh-failed").lastError)
		assertEquals(900L, byId.getValue("fresh-failed").updatedAt)

		assertEquals(100L, byId.getValue("already-pending").updatedAt)
	}

	private fun pendingMutation(
		mutationId: String,
		storeId: String = "evaluations",
		scopeKey: String = "scope-1",
		replaceKey: String = mutationId,
		status: String = "Pending",
		createdAt: Long = 1L,
		updatedAt: Long = createdAt,
		lastError: String? = null
	) = PendingMutationEntity(
		mutationId = mutationId,
		storeId = storeId,
		scopeKey = scopeKey,
		entityType = "Evaluation",
		entityId = "entity-$mutationId",
		replaceKey = replaceKey,
		payload = """{"value":1}""",
		preconditionType = "None",
		expectedRevision = 0L,
		status = status,
		createdAt = createdAt,
		updatedAt = updatedAt,
		lastError = lastError
	)
}
