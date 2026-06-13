package com.gdavidpb.tuindice.persistence.data.room.daos

import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.entity.PensumCacheEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PensumCacheDaoTest {
	private lateinit var database: TuIndiceDatabase
	private lateinit var dao: PensumCacheDao

	@BeforeTest
	fun setUp() {
		database = createInMemoryTuIndiceDatabase()
		dao = database.pensumCache
	}

	@AfterTest
	fun tearDown() {
		database.close()
	}

	@Test
	fun getPensum_byCacheKey_whenMissing_returnsNull() = runTest {
		assertNull(dao.getPensum(cacheKey = "missing"))
	}

	@Test
	fun upsertEntity_thenGetPensumByCacheKey_returnsRoundTrippedEntity() = runTest {
		val pensum = pensumCache()

		dao.upsertEntity(pensum)

		assertEquals(pensum, dao.getPensum(cacheKey = pensum.cacheKey))
	}

	@Test
	fun getPensum_byYearAndModality_returnsOnlyMatchingRow() = runTest {
		val presencial2017 = pensumCache(cacheKey = "2017:presencial", year = 2017, modalityId = "presencial")
		val virtual2017 = pensumCache(cacheKey = "2017:virtual", year = 2017, modalityId = "virtual")
		val presencial2012 = pensumCache(cacheKey = "2012:presencial", year = 2012, modalityId = "presencial")

		dao.upsertEntities(listOf(presencial2017, virtual2017, presencial2012))

		assertEquals(virtual2017, dao.getPensum(year = 2017, modalityId = "virtual"))
		assertEquals(presencial2012, dao.getPensum(year = 2012, modalityId = "presencial"))
		assertNull(dao.getPensum(year = 2025, modalityId = "presencial"))
	}

	@Test
	fun upsertEntity_withExistingCacheKey_replacesRow() = runTest {
		val original = pensumCache(updatedAt = 1L)
		val updated = original.copy(
			payloadJson = """{"subjects":[{"code":"ma1111"}]}""",
			updatedAt = 2L
		)

		dao.upsertEntity(original)
		dao.upsertEntity(updated)

		assertEquals(updated, dao.getPensum(cacheKey = original.cacheKey))
	}

	@Test
	fun observePensum_emitsMatchingRow() = runTest {
		val pensum = pensumCache()

		assertNull(dao.observePensum(cacheKey = pensum.cacheKey).first())

		dao.upsertEntity(pensum)

		assertEquals(pensum, dao.observePensum(cacheKey = pensum.cacheKey).first())
		assertNull(dao.observePensum(cacheKey = "other").first())
	}

	@Test
	fun deleteAll_removesEveryRow() = runTest {
		val presencial = pensumCache(cacheKey = "2017:presencial", year = 2017, modalityId = "presencial")
		val virtual = pensumCache(cacheKey = "2017:virtual", year = 2017, modalityId = "virtual")

		dao.upsertEntities(listOf(presencial, virtual))

		val deleted = dao.deleteAll()

		assertEquals(2, deleted)
		assertNull(dao.getPensum(cacheKey = presencial.cacheKey))
		assertNull(dao.getPensum(cacheKey = virtual.cacheKey))
	}

	private fun pensumCache(
		cacheKey: String = "2017:presencial",
		year: Int = 2017,
		modalityId: String = "presencial",
		payloadJson: String = """{"subjects":[]}""",
		updatedAt: Long = 1L
	) = PensumCacheEntity(
		cacheKey = cacheKey,
		year = year,
		modalityId = modalityId,
		payloadJson = payloadJson,
		updatedAt = updatedAt
	)
}
