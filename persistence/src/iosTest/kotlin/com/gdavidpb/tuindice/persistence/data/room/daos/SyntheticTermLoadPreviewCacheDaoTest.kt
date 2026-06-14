package com.gdavidpb.tuindice.persistence.data.room.daos

import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.entity.SyntheticTermLoadPreviewCacheEntity
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SyntheticTermLoadPreviewCacheDaoTest {
	private lateinit var database: TuIndiceDatabase
	private lateinit var dao: SyntheticTermLoadPreviewCacheDao

	@BeforeTest
	fun setUp() {
		database = createInMemoryTuIndiceDatabase()
		dao = database.syntheticTermLoadPreviewCache
	}

	@AfterTest
	fun tearDown() {
		database.close()
	}

	@Test
	fun getByCacheKey_whenMissing_returnsNull() = runTest {
		assertNull(dao.getByCacheKey(cacheKey = "missing"))
	}

	@Test
	fun upsertEntity_thenGetByCacheKey_returnsRoundTrippedEntity() = runTest {
		val preview = preview(cacheKey = "preview-1")

		dao.upsertEntity(preview)

		assertEquals(preview, dao.getByCacheKey(cacheKey = preview.cacheKey))
	}

	@Test
	fun upsertEntity_withExistingCacheKey_replacesRow() = runTest {
		val original = preview(cacheKey = "preview-1", available = true, updatedAt = 1L)
		val updated = original.copy(
			available = false,
			reason = "InsufficientData",
			updatedAt = 2L
		)

		dao.upsertEntity(original)
		dao.upsertEntity(updated)

		assertEquals(updated, dao.getByCacheKey(cacheKey = original.cacheKey))
	}

	@Test
	fun getFresh_whenRowIsFresh_returnsRow() = runTest {
		val preview = preview(cacheKey = "preview-1", expiresAt = 10L)

		dao.upsertEntity(preview)

		assertEquals(preview, dao.getFresh(cacheKey = preview.cacheKey, now = 9L))
	}

	@Test
	fun getFresh_whenRowExpired_returnsNull() = runTest {
		val preview = preview(cacheKey = "preview-1", expiresAt = 10L)

		dao.upsertEntity(preview)

		assertNull(dao.getFresh(cacheKey = preview.cacheKey, now = 10L))
		assertNull(dao.getFresh(cacheKey = preview.cacheKey, now = 11L))
	}

	@Test
	fun deleteExpired_removesRowsExpiredAtOrBeforeNow() = runTest {
		val expired = preview(cacheKey = "preview-expired", expiresAt = 5L)
		val fresh = preview(cacheKey = "preview-fresh", expiresAt = 10L)

		dao.upsertEntities(listOf(expired, fresh))

		val deleted = dao.deleteExpired(now = 5L)

		assertEquals(1, deleted)
		assertNull(dao.getByCacheKey(cacheKey = expired.cacheKey))
		assertEquals(fresh, dao.getByCacheKey(cacheKey = fresh.cacheKey))
	}

	@Test
	fun deleteAll_removesEveryRow() = runTest {
		val first = preview(cacheKey = "preview-1")
		val second = preview(cacheKey = "preview-2", termKey = "term-2024-2")

		dao.upsertEntities(listOf(first, second))

		val deleted = dao.deleteAll()

		assertEquals(2, deleted)
		assertNull(dao.getByCacheKey(cacheKey = first.cacheKey))
		assertNull(dao.getByCacheKey(cacheKey = second.cacheKey))
	}

	private fun preview(
		cacheKey: String,
		termKey: String = "term-2024-1",
		subjectCodesKey: String = "ci2125+ma2115",
		available: Boolean = true,
		updatedAt: Long = 1L,
		expiresAt: Long = 10L
	) = SyntheticTermLoadPreviewCacheEntity(
		cacheKey = cacheKey,
		termKey = termKey,
		subjectCodesKey = subjectCodesKey,
		available = available,
		reason = null,
		band = "Moderate",
		credits = 8,
		weightedDifficulty = 3.2,
		loadIndex = 0.64,
		baselineLoadIndex = 0.5,
		effectiveTerms = 6,
		basis = "History",
		confidence = "High",
		detail = null,
		updatedAt = updatedAt,
		expiresAt = expiresAt
	)
}
