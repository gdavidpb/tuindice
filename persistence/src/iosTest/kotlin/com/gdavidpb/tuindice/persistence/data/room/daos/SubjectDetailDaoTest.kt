package com.gdavidpb.tuindice.persistence.data.room.daos

import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectDetailEntity
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SubjectDetailDaoTest {
	private lateinit var database: TuIndiceDatabase
	private lateinit var dao: SubjectDetailDao

	@BeforeTest
	fun setUp() {
		database = createInMemoryTuIndiceDatabase()
		dao = database.subjectDetails
	}

	@AfterTest
	fun tearDown() {
		database.close()
	}

	@Test
	fun getSubjectDetail_whenMissing_returnsNull() = runTest {
		assertNull(dao.getSubjectDetail(subjectCode = "missing"))
	}

	@Test
	fun upsertEntity_thenGetSubjectDetail_returnsRoundTrippedEntity() = runTest {
		val detail = subjectDetail(subjectCode = "ma1111")

		dao.upsertEntity(detail)

		assertEquals(detail, dao.getSubjectDetail(subjectCode = detail.subjectCode))
	}

	@Test
	fun upsertEntity_withExistingSubjectCode_replacesRow() = runTest {
		val original = subjectDetail(
			subjectCode = "ma1111",
			name = null,
			credits = null,
			cacheStatus = "Pending"
		)
		val updated = original.copy(
			name = "Matematicas I",
			credits = 4,
			cacheStatus = "Ready",
			generatedAt = 2L,
			expiresAt = 20L
		)

		dao.upsertEntity(original)
		dao.upsertEntity(updated)

		assertEquals(updated, dao.getSubjectDetail(subjectCode = original.subjectCode))
	}

	@Test
	fun deleteBySubjectCode_removesOnlyMatchingRow() = runTest {
		val target = subjectDetail(subjectCode = "ma1111")
		val other = subjectDetail(subjectCode = "cs1111", name = "Lenguaje I")

		dao.upsertEntities(listOf(target, other))

		val deleted = dao.deleteBySubjectCode(subjectCode = target.subjectCode)

		assertEquals(1, deleted)
		assertNull(dao.getSubjectDetail(subjectCode = target.subjectCode))
		assertEquals(other, dao.getSubjectDetail(subjectCode = other.subjectCode))
	}

	@Test
	fun deleteBySubjectCode_whenMissing_deletesNothing() = runTest {
		dao.upsertEntity(subjectDetail(subjectCode = "ma1111"))

		assertEquals(0, dao.deleteBySubjectCode(subjectCode = "missing"))
	}

	@Test
	fun deleteAll_removesEveryRow() = runTest {
		dao.upsertEntities(
			listOf(
				subjectDetail(subjectCode = "ma1111"),
				subjectDetail(subjectCode = "cs1111", name = "Lenguaje I")
			)
		)

		val deleted = dao.deleteAll()

		assertEquals(2, deleted)
		assertNull(dao.getSubjectDetail(subjectCode = "ma1111"))
		assertNull(dao.getSubjectDetail(subjectCode = "cs1111"))
	}

	private fun subjectDetail(
		subjectCode: String,
		name: String? = "Matematicas I",
		credits: Int? = 4,
		cacheStatus: String = "Ready",
		generatedAt: Long = 1L,
		expiresAt: Long = 10L
	) = SubjectDetailEntity(
		subjectCode = subjectCode,
		name = name,
		credits = credits,
		gradingMode = "Numeric",
		cacheStatus = cacheStatus,
		generatedAt = generatedAt,
		expiresAt = expiresAt
	)
}
