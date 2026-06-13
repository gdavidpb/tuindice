package com.gdavidpb.tuindice.persistence.data.room.daos

import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectStatsSegmentEntity
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SubjectStatsSegmentDaoTest {
	private lateinit var database: TuIndiceDatabase
	private lateinit var dao: SubjectStatsSegmentDao

	@BeforeTest
	fun setUp() {
		database = createInMemoryTuIndiceDatabase()
		dao = database.subjectStatsSegments
	}

	@AfterTest
	fun tearDown() {
		database.close()
	}

	@Test
	fun getSubjectSegments_whenEmpty_returnsEmptyList() = runTest {
		assertEquals(emptyList(), dao.getSubjectSegments(subjectCode = "ma1111"))
	}

	@Test
	fun upsertEntity_thenGetSubjectSegments_returnsRoundTrippedEntity() = runTest {
		val segment = segment(id = "segment-1")

		dao.upsertEntity(segment)

		assertEquals(listOf(segment), dao.getSubjectSegments(subjectCode = segment.subjectCode))
	}

	@Test
	fun getSubjectSegments_returnsOnlyMatchingSubject() = runTest {
		val overall = segment(id = "segment-1", subjectCode = "ma1111")
		val yearSegment = segment(
			id = "segment-2",
			subjectCode = "ma1111",
			segmentType = "year",
			segmentKey = 2024
		)
		val otherSubject = segment(id = "segment-3", subjectCode = "cs1111")

		dao.upsertEntities(listOf(overall, yearSegment, otherSubject))

		assertEquals(
			setOf(overall, yearSegment),
			dao.getSubjectSegments(subjectCode = "ma1111").toSet()
		)
		assertEquals(
			listOf(otherSubject),
			dao.getSubjectSegments(subjectCode = "cs1111")
		)
	}

	@Test
	fun upsertEntity_withExistingId_replacesRow() = runTest {
		val original = segment(id = "segment-1", status = "Pending", averageGrade = null)
		val updated = original.copy(status = "Ready", averageGrade = 12.5, generatedAt = 2L)

		dao.upsertEntity(original)
		dao.upsertEntity(updated)

		assertEquals(listOf(updated), dao.getSubjectSegments(subjectCode = original.subjectCode))
	}

	@Test
	fun deleteBySubjectCode_removesOnlyMatchingRows() = runTest {
		val overall = segment(id = "segment-1", subjectCode = "ma1111")
		val yearSegment = segment(
			id = "segment-2",
			subjectCode = "ma1111",
			segmentType = "year",
			segmentKey = 2024
		)
		val otherSubject = segment(id = "segment-3", subjectCode = "cs1111")

		dao.upsertEntities(listOf(overall, yearSegment, otherSubject))

		val deleted = dao.deleteBySubjectCode(subjectCode = "ma1111")

		assertEquals(2, deleted)
		assertEquals(emptyList(), dao.getSubjectSegments(subjectCode = "ma1111"))
		assertEquals(listOf(otherSubject), dao.getSubjectSegments(subjectCode = "cs1111"))
	}

	@Test
	fun deleteAll_removesEveryRow() = runTest {
		dao.upsertEntities(
			listOf(
				segment(id = "segment-1", subjectCode = "ma1111"),
				segment(id = "segment-2", subjectCode = "cs1111")
			)
		)

		val deleted = dao.deleteAll()

		assertEquals(2, deleted)
		assertEquals(emptyList(), dao.getSubjectSegments(subjectCode = "ma1111"))
		assertEquals(emptyList(), dao.getSubjectSegments(subjectCode = "cs1111"))
	}

	private fun segment(
		id: String,
		subjectCode: String = "ma1111",
		segmentType: String = "overall",
		segmentKey: Int? = null,
		status: String = "Ready",
		averageGrade: Double? = 12.5,
		generatedAt: Long = 1L
	) = SubjectStatsSegmentEntity(
		id = id,
		subjectCode = subjectCode,
		segmentType = segmentType,
		segmentKey = segmentKey,
		status = status,
		generatedAt = generatedAt,
		expiresAt = 10L,
		sampleStudents = 120,
		closedAttempts = 150,
		numericLatestStudents = 110,
		latestApprovedCount = 80,
		latestFailedCount = 25,
		latestRetiredCount = 10,
		latestUnreportedCount = 5,
		averageGrade = averageGrade,
		medianGrade = 13.0,
		stddevGrade = 3.2,
		firstAttemptPassRate = 0.66,
		approvalRate = 0.72,
		latestFailureRate = 0.22,
		latestWithdrawalRate = 0.08,
		retakeRate = 0.25,
		avgAttemptsToPass = 1.4,
		medianAttemptsToPass = 1.0,
		difficultyScore = 62,
		difficultyBand = "Moderate",
		firstClosedTermStartAt = 100L,
		lastClosedTermStartAt = 900L
	)
}
