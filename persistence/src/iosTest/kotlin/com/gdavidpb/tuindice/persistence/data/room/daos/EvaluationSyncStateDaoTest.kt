package com.gdavidpb.tuindice.persistence.data.room.daos

import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.entity.EvaluationSyncStateEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EvaluationSyncStateDaoTest {
	private lateinit var database: TuIndiceDatabase
	private lateinit var dao: EvaluationSyncStateDao

	@BeforeTest
	fun setUp() {
		database = createInMemoryTuIndiceDatabase()
		dao = database.evaluationSyncState
	}

	@AfterTest
	fun tearDown() {
		database.close()
	}

	@Test
	fun getSyncState_whenEmpty_returnsNull() = runTest {
		assertNull(dao.getSyncState())
	}

	@Test
	fun observeSyncState_whenEmpty_emitsNull() = runTest {
		assertNull(dao.observeSyncState().first())
	}

	@Test
	fun upsertEntity_thenGetSyncState_returnsRoundTrippedEntity() = runTest {
		val syncState = EvaluationSyncStateEntity(hasSynced = true)

		dao.upsertEntity(syncState)

		assertEquals(syncState, dao.getSyncState())
	}

	@Test
	fun upsertEntity_withExistingKey_replacesRow() = runTest {
		val original = EvaluationSyncStateEntity(hasSynced = false)
		val updated = original.copy(hasSynced = true)

		dao.upsertEntity(original)
		dao.upsertEntity(updated)

		assertEquals(updated, dao.getSyncState())
	}

	@Test
	fun getSyncState_filtersByKey() = runTest {
		val defaultState = EvaluationSyncStateEntity(hasSynced = true)
		val otherState = EvaluationSyncStateEntity(key = "other-evaluations", hasSynced = false)

		dao.upsertEntities(listOf(defaultState, otherState))

		assertEquals(defaultState, dao.getSyncState())
		assertEquals(otherState, dao.getSyncState(key = "other-evaluations"))
		assertNull(dao.getSyncState(key = "missing"))
	}

	@Test
	fun observeSyncState_emitsUpsertedRow() = runTest {
		val syncState = EvaluationSyncStateEntity(hasSynced = true)

		dao.upsertEntity(syncState)

		assertEquals(syncState, dao.observeSyncState().first())
	}

	@Test
	fun deleteAll_removesEveryRow() = runTest {
		dao.upsertEntities(
			listOf(
				EvaluationSyncStateEntity(hasSynced = true),
				EvaluationSyncStateEntity(key = "other-evaluations", hasSynced = false)
			)
		)

		dao.deleteAll()

		assertNull(dao.getSyncState())
		assertNull(dao.getSyncState(key = "other-evaluations"))
	}
}
