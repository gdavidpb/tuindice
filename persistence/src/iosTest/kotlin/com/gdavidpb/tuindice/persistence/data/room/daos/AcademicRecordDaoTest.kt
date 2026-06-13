package com.gdavidpb.tuindice.persistence.data.room.daos

import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicRecordEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AcademicRecordDaoTest {
	private lateinit var database: TuIndiceDatabase
	private lateinit var dao: AcademicRecordDao

	@BeforeTest
	fun setUp() {
		database = createInMemoryTuIndiceDatabase()
		dao = database.academicRecords
	}

	@AfterTest
	fun tearDown() {
		database.close()
	}

	@Test
	fun getRecord_whenEmpty_returnsNull() = runTest {
		assertNull(dao.getRecord())
	}

	@Test
	fun observeRecordFlow_whenEmpty_emitsNull() = runTest {
		assertNull(dao.observeRecordFlow().first())
	}

	@Test
	fun upsertEntity_thenGetRecord_returnsRoundTrippedEntity() = runTest {
		val record = record()

		dao.upsertEntity(record)

		assertEquals(record, dao.getRecord())
	}

	@Test
	fun upsertEntity_withExistingId_replacesRow() = runTest {
		val original = record(revision = 1L, updatedAt = 1L)
		val updated = original.copy(revision = 2L, updatedAt = 2L)

		dao.upsertEntity(original)
		dao.upsertEntity(updated)

		assertEquals(updated, dao.getRecord())
	}

	@Test
	fun observeRecordFlow_emitsUpsertedRecord() = runTest {
		val record = record()

		dao.upsertEntity(record)

		assertEquals(record, dao.observeRecordFlow().first())
	}

	@Test
	fun deleteAll_removesEveryRow() = runTest {
		dao.upsertEntity(record())

		dao.deleteAll()

		assertNull(dao.getRecord())
		assertNull(dao.observeRecordFlow().first())
	}

	private fun record(
		id: String = "record-1",
		revision: Long = 1L,
		updatedAt: Long = 1L
	) = AcademicRecordEntity(
		id = id,
		revision = revision,
		updatedAt = updatedAt
	)
}
