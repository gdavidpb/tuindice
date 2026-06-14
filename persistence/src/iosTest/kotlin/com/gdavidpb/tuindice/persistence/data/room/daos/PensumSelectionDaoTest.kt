package com.gdavidpb.tuindice.persistence.data.room.daos

import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.entity.PensumSelectionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PensumSelectionDaoTest {
	private lateinit var database: TuIndiceDatabase
	private lateinit var dao: PensumSelectionDao

	@BeforeTest
	fun setUp() {
		database = createInMemoryTuIndiceDatabase()
		dao = database.pensumSelection
	}

	@AfterTest
	fun tearDown() {
		database.close()
	}

	@Test
	fun getSelection_whenEmpty_returnsNull() = runTest {
		assertNull(dao.getSelection())
	}

	@Test
	fun observeSelection_whenEmpty_emitsNull() = runTest {
		assertNull(dao.observeSelection().first())
	}

	@Test
	fun upsertEntity_thenGetSelection_returnsRoundTrippedEntity() = runTest {
		val selection = selection()

		dao.upsertEntity(selection)

		assertEquals(selection, dao.getSelection())
	}

	@Test
	fun upsertEntity_withExistingId_replacesRow() = runTest {
		val original = selection(year = 2017, modalityId = "presencial", cacheKey = "2017:presencial")
		val updated = original.copy(
			year = 2012,
			cacheKey = "2012:presencial",
			updatedAt = 2L
		)

		dao.upsertEntity(original)
		dao.upsertEntity(updated)

		assertEquals(updated, dao.getSelection())
	}

	@Test
	fun getSelection_filtersById() = runTest {
		dao.upsertEntity(selection())

		assertNull(dao.getSelection(id = "other"))
	}

	@Test
	fun observeSelection_emitsUpsertedSelection() = runTest {
		val selection = selection()

		dao.upsertEntity(selection)

		assertEquals(selection, dao.observeSelection().first())
	}

	@Test
	fun deleteAll_removesEveryRow() = runTest {
		dao.upsertEntity(selection())

		val deleted = dao.deleteAll()

		assertEquals(1, deleted)
		assertNull(dao.getSelection())
	}

	private fun selection(
		year: Int? = 2017,
		modalityId: String? = "presencial",
		cacheKey: String? = "2017:presencial",
		updatedAt: Long = 1L
	) = PensumSelectionEntity(
		year = year,
		modalityId = modalityId,
		cacheKey = cacheKey,
		updatedAt = updatedAt
	)
}
