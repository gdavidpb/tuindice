package com.gdavidpb.tuindice.subjects.data.mapper

import com.gdavidpb.tuindice.base.domain.model.GradingMode
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectDetailEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectStatsAttemptBinEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectStatsGradeBinEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectStatsSegmentEntity
import com.gdavidpb.tuindice.subjects.domain.model.SubjectAttemptBin
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetailResult
import com.gdavidpb.tuindice.subjects.domain.model.SubjectGradeBin
import com.gdavidpb.tuindice.subjects.testing.readySubjectDetail
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SubjectMappersTest {
	@Test
	fun toSubjectDetailResult_whenCacheStatusIsInsufficient_thenReturnsUnavailable() {
		val entity = SubjectDetailEntity(
			subjectCode = "MAT404",
			cacheStatus = CACHE_STATUS_INSUFFICIENT,
			generatedAt = 123L,
			expiresAt = 456L
		)

		val result = entity.toSubjectDetailResult(
			segments = emptyList(),
			gradeBins = emptyList(),
			attemptBins = emptyList()
		)

		assertEquals(
			SubjectDetailResult.Unavailable(subjectCode = "MAT404", expiresAt = 456L),
			result
		)
	}

	@Test
	fun toSubjectDetailResult_whenSegmentsAreReady_thenScopesAndSortsBinsPerSegment() {
		val entity = subjectDetailEntity()
		val segments = listOf(
			segmentEntity(segmentType = SEGMENT_TYPE_CAREER, sampleStudents = 18),
			segmentEntity(segmentType = SEGMENT_TYPE_GLOBAL, sampleStudents = 240)
		)
		val gradeBins = listOf(
			gradeBinEntity(segmentType = SEGMENT_TYPE_CAREER, series = SERIES_LATEST, grade = 5, count = 3),
			gradeBinEntity(segmentType = SEGMENT_TYPE_CAREER, series = SERIES_LATEST, grade = 1, count = 1),
			gradeBinEntity(segmentType = SEGMENT_TYPE_CAREER, series = SERIES_ALL, grade = 3, count = 7),
			gradeBinEntity(segmentType = SEGMENT_TYPE_GLOBAL, series = SERIES_LATEST, grade = 2, count = 9),
			gradeBinEntity(segmentType = SEGMENT_TYPE_GLOBAL, series = SERIES_ALL, grade = 4, count = 11)
		)
		val attemptBins = listOf(
			attemptBinEntity(segmentType = SEGMENT_TYPE_CAREER, bucket = "3_plus", count = 2),
			attemptBinEntity(segmentType = SEGMENT_TYPE_CAREER, bucket = "1", count = 8),
			attemptBinEntity(segmentType = SEGMENT_TYPE_CAREER, bucket = "2", count = 4),
			attemptBinEntity(segmentType = SEGMENT_TYPE_GLOBAL, bucket = "2", count = 5)
		)

		val result = entity.toSubjectDetailResult(
			segments = segments,
			gradeBins = gradeBins,
			attemptBins = attemptBins
		)

		val ready = assertIs<SubjectDetailResult.Ready>(result)
		val career = assertNotNull(ready.detail.careerSegment)
		val global = assertNotNull(ready.detail.globalSegment)

		assertEquals(18, career.sampleStudents)
		assertEquals(240, global.sampleStudents)

		assertEquals(
			listOf(
				SubjectGradeBin(grade = 1, count = 1),
				SubjectGradeBin(grade = 5, count = 3)
			),
			career.latestGradeBins
		)
		assertEquals(
			listOf(SubjectGradeBin(grade = 3, count = 7)),
			career.allGradeBins
		)
		assertEquals(
			listOf(
				SubjectAttemptBin(bucket = "1", count = 8),
				SubjectAttemptBin(bucket = "2", count = 4),
				SubjectAttemptBin(bucket = "3_plus", count = 2)
			),
			career.attemptsToPassBins
		)

		assertEquals(
			listOf(SubjectGradeBin(grade = 2, count = 9)),
			global.latestGradeBins
		)
		assertEquals(
			listOf(SubjectGradeBin(grade = 4, count = 11)),
			global.allGradeBins
		)
		assertEquals(
			listOf(SubjectAttemptBin(bucket = "2", count = 5)),
			global.attemptsToPassBins
		)
	}

	@Test
	fun toSubjectDetailResult_whenSegmentIsNotReady_thenIgnoresThatSegment() {
		val entity = subjectDetailEntity()
		val segments = listOf(
			segmentEntity(segmentType = SEGMENT_TYPE_CAREER, sampleStudents = 18),
			segmentEntity(segmentType = SEGMENT_TYPE_GLOBAL, sampleStudents = 240, status = "PENDING")
		)

		val result = entity.toSubjectDetailResult(
			segments = segments,
			gradeBins = emptyList(),
			attemptBins = emptyList()
		)

		val ready = assertIs<SubjectDetailResult.Ready>(result)
		assertNotNull(ready.detail.careerSegment)
		assertNull(ready.detail.globalSegment)
	}

	@Test
	fun toSubjectDetailResult_whenNoReadySegmentsExist_thenReturnsNull() {
		val entity = subjectDetailEntity()

		val result = entity.toSubjectDetailResult(
			segments = listOf(
				segmentEntity(segmentType = SEGMENT_TYPE_CAREER, sampleStudents = 18, status = "PENDING")
			),
			gradeBins = emptyList(),
			attemptBins = emptyList()
		)

		assertNull(result)
	}

	@Test
	fun toSubjectDetailResult_whenSubjectFieldsAreMissing_thenReturnsNull() {
		val segments = listOf(
			segmentEntity(segmentType = SEGMENT_TYPE_CAREER, sampleStudents = 18)
		)

		assertNull(
			subjectDetailEntity(name = null).toSubjectDetailResult(
				segments = segments,
				gradeBins = emptyList(),
				attemptBins = emptyList()
			)
		)
		assertNull(
			subjectDetailEntity(credits = null).toSubjectDetailResult(
				segments = segments,
				gradeBins = emptyList(),
				attemptBins = emptyList()
			)
		)
		assertNull(
			subjectDetailEntity(gradingMode = null).toSubjectDetailResult(
				segments = segments,
				gradeBins = emptyList(),
				attemptBins = emptyList()
			)
		)
	}

	@Test
	fun toSubjectDetailEntity_whenResultIsReady_thenPersistsReadyCacheStatus() {
		val ready = readySubjectDetail(subjectCode = "MAT101", expiresAt = 456L)

		val entity = ready.toSubjectDetailEntity()

		assertEquals("MAT101", entity.subjectCode)
		assertEquals("Calculo I", entity.name)
		assertEquals(5, entity.credits)
		assertEquals(GradingMode.NUMERIC.name, entity.gradingMode)
		assertEquals(CACHE_STATUS_READY, entity.cacheStatus)
		assertEquals(ready.detail.generatedAt, entity.generatedAt)
		assertEquals(456L, entity.expiresAt)
	}

	@Test
	fun toSubjectDetailEntity_whenResultIsUnavailable_thenPersistsInsufficientCacheStatus() {
		val unavailable = SubjectDetailResult.Unavailable(
			subjectCode = "MAT404",
			expiresAt = 456L
		)

		val entity = unavailable.toSubjectDetailEntity()

		assertEquals("MAT404", entity.subjectCode)
		assertNull(entity.name)
		assertNull(entity.credits)
		assertNull(entity.gradingMode)
		assertEquals(CACHE_STATUS_INSUFFICIENT, entity.cacheStatus)
		assertEquals(456L, entity.generatedAt)
		assertEquals(456L, entity.expiresAt)
	}

	@Test
	fun toEntities_whenDetailHasBothSegments_thenExpandsBinsBySegmentAndSeries() {
		val detail = detailWithBothSegments()

		val segmentEntities = detail.toSegmentEntities()
		val gradeBinEntities = detail.toGradeBinEntities()
		val attemptBinEntities = detail.toAttemptBinEntities()

		assertEquals(
			listOf("MAT101:$SEGMENT_TYPE_CAREER", "MAT101:$SEGMENT_TYPE_GLOBAL"),
			segmentEntities.map(SubjectStatsSegmentEntity::id)
		)
		assertEquals(
			listOf(SEGMENT_STATUS_READY, SEGMENT_STATUS_READY),
			segmentEntities.map(SubjectStatsSegmentEntity::status)
		)

		assertEquals(
			2,
			gradeBinEntities.count { bin ->
				bin.segmentType == SEGMENT_TYPE_CAREER && bin.series == SERIES_LATEST
			}
		)
		assertEquals(
			2,
			gradeBinEntities.count { bin ->
				bin.segmentType == SEGMENT_TYPE_CAREER && bin.series == SERIES_ALL
			}
		)
		assertEquals(
			1,
			gradeBinEntities.count { bin ->
				bin.segmentType == SEGMENT_TYPE_GLOBAL && bin.series == SERIES_LATEST
			}
		)
		assertEquals(
			1,
			gradeBinEntities.count { bin ->
				bin.segmentType == SEGMENT_TYPE_GLOBAL && bin.series == SERIES_ALL
			}
		)

		assertEquals(
			listOf(SEGMENT_TYPE_CAREER, SEGMENT_TYPE_CAREER, SEGMENT_TYPE_GLOBAL),
			attemptBinEntities.map(SubjectStatsAttemptBinEntity::segmentType)
		)
	}

	@Test
	fun toEntities_whenRoundTrippedThroughPersistenceModel_thenRestoresOriginalDetail() {
		val detail = detailWithBothSegments()
		val ready = SubjectDetailResult.Ready(detail = detail)

		val rebuilt = ready.toSubjectDetailEntity().toSubjectDetailResult(
			segments = detail.toSegmentEntities(),
			gradeBins = detail.toGradeBinEntities(),
			attemptBins = detail.toAttemptBinEntities()
		)

		assertEquals(ready, rebuilt)
	}

	private fun detailWithBothSegments() = readySubjectDetail(
		subjectCode = "MAT101",
		expiresAt = 456L
	).detail.let { detail ->
		detail.copy(
			globalSegment = detail.careerSegment?.copy(
				sampleStudents = 240,
				latestGradeBins = listOf(SubjectGradeBin(grade = 2, count = 6)),
				allGradeBins = listOf(SubjectGradeBin(grade = 2, count = 7)),
				attemptsToPassBins = listOf(SubjectAttemptBin(bucket = "2", count = 5))
			)
		)
	}

	private fun subjectDetailEntity(
		subjectCode: String = "MAT101",
		name: String? = "Calculo I",
		credits: Int? = 5,
		gradingMode: String? = GradingMode.NUMERIC.name
	) = SubjectDetailEntity(
		subjectCode = subjectCode,
		name = name,
		credits = credits,
		gradingMode = gradingMode,
		cacheStatus = CACHE_STATUS_READY,
		generatedAt = 123L,
		expiresAt = 456L
	)

	private fun segmentEntity(
		segmentType: String,
		sampleStudents: Int,
		status: String = SEGMENT_STATUS_READY,
		subjectCode: String = "MAT101"
	) = SubjectStatsSegmentEntity(
		id = "$subjectCode:$segmentType",
		subjectCode = subjectCode,
		segmentType = segmentType,
		status = status,
		generatedAt = 123L,
		expiresAt = 456L,
		sampleStudents = sampleStudents,
		closedAttempts = 24,
		numericLatestStudents = 18,
		latestApprovedCount = 12,
		latestFailedCount = 4,
		latestRetiredCount = 1,
		latestUnreportedCount = 1
	)

	private fun gradeBinEntity(
		segmentType: String,
		series: String,
		grade: Int,
		count: Int,
		subjectCode: String = "MAT101"
	) = SubjectStatsGradeBinEntity(
		id = "$subjectCode:$segmentType:none:$series:$grade",
		subjectCode = subjectCode,
		segmentType = segmentType,
		series = series,
		grade = grade,
		count = count,
		generatedAt = 123L
	)

	private fun attemptBinEntity(
		segmentType: String,
		bucket: String,
		count: Int,
		subjectCode: String = "MAT101"
	) = SubjectStatsAttemptBinEntity(
		id = "$subjectCode:$segmentType:none:$bucket",
		subjectCode = subjectCode,
		segmentType = segmentType,
		bucket = bucket,
		count = count,
		generatedAt = 123L
	)
}
