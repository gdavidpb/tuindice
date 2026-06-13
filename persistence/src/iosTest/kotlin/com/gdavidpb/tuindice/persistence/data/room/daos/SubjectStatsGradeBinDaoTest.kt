package com.gdavidpb.tuindice.persistence.data.room.daos

import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectStatsGradeBinEntity
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SubjectStatsGradeBinDaoTest {
	private lateinit var database: TuIndiceDatabase
	private lateinit var dao: SubjectStatsGradeBinDao

	@BeforeTest
	fun setUp() {
		database = createInMemoryTuIndiceDatabase()
		dao = database.subjectStatsGradeBins
	}

	@AfterTest
	fun tearDown() {
		database.close()
	}

	@Test
	fun getSubjectGradeBins_whenEmpty_returnsEmptyList() = runTest {
		assertEquals(emptyList(), dao.getSubjectGradeBins(subjectCode = "ma1111"))
	}

	@Test
	fun upsertEntity_thenGetSubjectGradeBins_returnsRoundTrippedEntity() = runTest {
		val bin = gradeBin(id = "bin-1")

		dao.upsertEntity(bin)

		assertEquals(listOf(bin), dao.getSubjectGradeBins(subjectCode = bin.subjectCode))
	}

	@Test
	fun getSubjectGradeBins_returnsOnlyMatchingSubject() = runTest {
		val gradeFourteen = gradeBin(id = "bin-1", subjectCode = "ma1111", grade = 14)
		val gradeFifteen = gradeBin(id = "bin-2", subjectCode = "ma1111", grade = 15)
		val otherSubject = gradeBin(id = "bin-3", subjectCode = "cs1111", grade = 14)

		dao.upsertEntities(listOf(gradeFourteen, gradeFifteen, otherSubject))

		assertEquals(
			setOf(gradeFourteen, gradeFifteen),
			dao.getSubjectGradeBins(subjectCode = "ma1111").toSet()
		)
		assertEquals(
			listOf(otherSubject),
			dao.getSubjectGradeBins(subjectCode = "cs1111")
		)
	}

	@Test
	fun upsertEntity_withExistingId_replacesRow() = runTest {
		val original = gradeBin(id = "bin-1", count = 7)
		val updated = original.copy(count = 9, generatedAt = 2L)

		dao.upsertEntity(original)
		dao.upsertEntity(updated)

		assertEquals(listOf(updated), dao.getSubjectGradeBins(subjectCode = original.subjectCode))
	}

	@Test
	fun deleteBySubjectCode_removesOnlyMatchingRows() = runTest {
		val target = gradeBin(id = "bin-1", subjectCode = "ma1111", grade = 14)
		val otherTarget = gradeBin(id = "bin-2", subjectCode = "ma1111", grade = 15)
		val kept = gradeBin(id = "bin-3", subjectCode = "cs1111", grade = 14)

		dao.upsertEntities(listOf(target, otherTarget, kept))

		val deleted = dao.deleteBySubjectCode(subjectCode = "ma1111")

		assertEquals(2, deleted)
		assertEquals(emptyList(), dao.getSubjectGradeBins(subjectCode = "ma1111"))
		assertEquals(listOf(kept), dao.getSubjectGradeBins(subjectCode = "cs1111"))
	}

	@Test
	fun deleteBySubjectCode_whenMissing_deletesNothing() = runTest {
		dao.upsertEntity(gradeBin(id = "bin-1"))

		assertEquals(0, dao.deleteBySubjectCode(subjectCode = "missing"))
	}

	@Test
	fun deleteAll_removesEveryRow() = runTest {
		dao.upsertEntities(
			listOf(
				gradeBin(id = "bin-1", subjectCode = "ma1111", grade = 14),
				gradeBin(id = "bin-2", subjectCode = "cs1111", grade = 15)
			)
		)

		val deleted = dao.deleteAll()

		assertEquals(2, deleted)
		assertEquals(emptyList(), dao.getSubjectGradeBins(subjectCode = "ma1111"))
		assertEquals(emptyList(), dao.getSubjectGradeBins(subjectCode = "cs1111"))
	}

	private fun gradeBin(
		id: String,
		subjectCode: String = "ma1111",
		segmentType: String = "overall",
		segmentKey: Int? = null,
		series: String = "latest",
		grade: Int = 15,
		count: Int = 7,
		generatedAt: Long = 1L
	) = SubjectStatsGradeBinEntity(
		id = id,
		subjectCode = subjectCode,
		segmentType = segmentType,
		segmentKey = segmentKey,
		series = series,
		grade = grade,
		count = count,
		generatedAt = generatedAt
	)
}
