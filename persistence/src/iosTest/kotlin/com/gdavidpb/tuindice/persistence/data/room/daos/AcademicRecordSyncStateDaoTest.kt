package com.gdavidpb.tuindice.persistence.data.room.daos

import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicRecordSyncStateEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AcademicRecordSyncStateDaoTest {
	private lateinit var database: TuIndiceDatabase
	private lateinit var dao: AcademicRecordSyncStateDao

	@BeforeTest
	fun setUp() {
		database = createInMemoryTuIndiceDatabase()
		dao = database.academicRecordSyncState
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
		val syncState = AcademicRecordSyncStateEntity(hasSynced = true)

		dao.upsertEntity(syncState)

		assertEquals(syncState, dao.getSyncState())
	}

	@Test
	fun upsertEntity_withExistingKey_replacesRow() = runTest {
		val original = AcademicRecordSyncStateEntity(hasSynced = false)
		val updated = original.copy(hasSynced = true)

		dao.upsertEntity(original)
		dao.upsertEntity(updated)

		assertEquals(updated, dao.getSyncState())
	}

	@Test
	fun getSyncState_filtersByKey() = runTest {
		val defaultState = AcademicRecordSyncStateEntity(hasSynced = true)
		val otherState = AcademicRecordSyncStateEntity(key = "other-record", hasSynced = false)

		dao.upsertEntities(listOf(defaultState, otherState))

		assertEquals(defaultState, dao.getSyncState())
		assertEquals(otherState, dao.getSyncState(key = "other-record"))
		assertNull(dao.getSyncState(key = "missing"))
	}

	@Test
	fun observeSyncState_emitsUpsertedRow() = runTest {
		val syncState = AcademicRecordSyncStateEntity(hasSynced = true)

		dao.upsertEntity(syncState)

		assertEquals(syncState, dao.observeSyncState().first())
	}

	@Test
	fun deleteAll_removesEveryRow() = runTest {
		dao.upsertEntities(
			listOf(
				AcademicRecordSyncStateEntity(hasSynced = true),
				AcademicRecordSyncStateEntity(key = "other-record", hasSynced = false)
			)
		)

		dao.deleteAll()

		assertNull(dao.getSyncState())
		assertNull(dao.getSyncState(key = "other-record"))
	}
}
