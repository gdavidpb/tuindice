package com.gdavidpb.tuindice.persistence.data.room.daos

import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectStatsAttemptBinEntity
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SubjectStatsAttemptBinDaoTest {
	private lateinit var database: TuIndiceDatabase
	private lateinit var dao: SubjectStatsAttemptBinDao

	@BeforeTest
	fun setUp() {
		database = createInMemoryTuIndiceDatabase()
		dao = database.subjectStatsAttemptBins
	}

	@AfterTest
	fun tearDown() {
		database.close()
	}

	@Test
	fun getSubjectAttemptBins_whenEmpty_returnsEmptyList() = runTest {
		assertEquals(emptyList(), dao.getSubjectAttemptBins(subjectCode = "ma1111"))
	}

	@Test
	fun upsertEntity_thenGetSubjectAttemptBins_returnsRoundTrippedEntity() = runTest {
		val bin = attemptBin(id = "bin-1")

		dao.upsertEntity(bin)

		assertEquals(listOf(bin), dao.getSubjectAttemptBins(subjectCode = bin.subjectCode))
	}

	@Test
	fun getSubjectAttemptBins_returnsOnlyMatchingSubject() = runTest {
		val firstBucket = attemptBin(id = "bin-1", subjectCode = "ma1111", bucket = "1")
		val secondBucket = attemptBin(id = "bin-2", subjectCode = "ma1111", bucket = "2")
		val otherSubject = attemptBin(id = "bin-3", subjectCode = "cs1111", bucket = "1")

		dao.upsertEntities(listOf(firstBucket, secondBucket, otherSubject))

		assertEquals(
			setOf(firstBucket, secondBucket),
			dao.getSubjectAttemptBins(subjectCode = "ma1111").toSet()
		)
		assertEquals(
			listOf(otherSubject),
			dao.getSubjectAttemptBins(subjectCode = "cs1111")
		)
	}

	@Test
	fun upsertEntity_withExistingId_replacesRow() = runTest {
		val original = attemptBin(id = "bin-1", count = 10)
		val updated = original.copy(count = 12, generatedAt = 2L)

		dao.upsertEntity(original)
		dao.upsertEntity(updated)

		assertEquals(listOf(updated), dao.getSubjectAttemptBins(subjectCode = original.subjectCode))
	}

	@Test
	fun deleteBySubjectCode_removesOnlyMatchingRows() = runTest {
		val target = attemptBin(id = "bin-1", subjectCode = "ma1111", bucket = "1")
		val otherTarget = attemptBin(id = "bin-2", subjectCode = "ma1111", bucket = "2")
		val kept = attemptBin(id = "bin-3", subjectCode = "cs1111", bucket = "1")

		dao.upsertEntities(listOf(target, otherTarget, kept))

		val deleted = dao.deleteBySubjectCode(subjectCode = "ma1111")

		assertEquals(2, deleted)
		assertEquals(emptyList(), dao.getSubjectAttemptBins(subjectCode = "ma1111"))
		assertEquals(listOf(kept), dao.getSubjectAttemptBins(subjectCode = "cs1111"))
	}

	@Test
	fun deleteBySubjectCode_whenMissing_deletesNothing() = runTest {
		dao.upsertEntity(attemptBin(id = "bin-1"))

		assertEquals(0, dao.deleteBySubjectCode(subjectCode = "missing"))
	}

	@Test
	fun deleteAll_removesEveryRow() = runTest {
		dao.upsertEntities(
			listOf(
				attemptBin(id = "bin-1", subjectCode = "ma1111", bucket = "1"),
				attemptBin(id = "bin-2", subjectCode = "cs1111", bucket = "3+")
			)
		)

		val deleted = dao.deleteAll()

		assertEquals(2, deleted)
		assertEquals(emptyList(), dao.getSubjectAttemptBins(subjectCode = "ma1111"))
		assertEquals(emptyList(), dao.getSubjectAttemptBins(subjectCode = "cs1111"))
	}

	private fun attemptBin(
		id: String,
		subjectCode: String = "ma1111",
		segmentType: String = "overall",
		segmentKey: Int? = null,
		bucket: String = "1",
		count: Int = 10,
		generatedAt: Long = 1L
	) = SubjectStatsAttemptBinEntity(
		id = id,
		subjectCode = subjectCode,
		segmentType = segmentType,
		segmentKey = segmentKey,
		bucket = bucket,
		count = count,
		generatedAt = generatedAt
	)
}
