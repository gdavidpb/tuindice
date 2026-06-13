package com.gdavidpb.tuindice.subjects.data.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumNodeStatus
import com.gdavidpb.tuindice.base.domain.model.GradingMode
import com.gdavidpb.tuindice.persistence.data.room.entity.SubjectCatalogCacheEntity
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSearchResult
import com.gdavidpb.tuindice.subjects.testing.readySubjectDetail
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SubjectCatalogMappersTest {
	@Test
	fun toSubjectCatalogCacheEntity_whenNameHasAccentsAndSpacing_thenStoresNormalizedSearchFields() {
		val result = SubjectSearchResult(
			subjectCode = "ma1111",
			name = "Matemáticas   Básicas Año I",
			credits = 4,
			gradingMode = GradingMode.NUMERIC
		)

		val entity = result.toSubjectCatalogCacheEntity(updatedAt = 123L)

		assertEquals("ma1111", entity.subjectCode)
		assertEquals("Matemáticas   Básicas Año I", entity.name)
		assertEquals(4, entity.credits)
		assertEquals(GradingMode.NUMERIC.name, entity.gradingMode)
		assertEquals("MA1111", entity.normalizedCode)
		assertEquals("MATEMATICAS BASICAS ANO I", entity.normalizedName)
		assertEquals(123L, entity.updatedAt)
	}

	@Test
	fun toSubjectSearchResult_whenEntityHasNoGradingMode_thenKeepsGradingModeUnknown() {
		val entity = SubjectCatalogCacheEntity(
			subjectCode = "EC5333",
			name = "INT. A LAS MICROONDAS",
			credits = 3,
			gradingMode = null,
			normalizedCode = "EC5333",
			normalizedName = "INT. A LAS MICROONDAS",
			updatedAt = 123L
		)

		val result = entity.toSubjectSearchResult()

		assertEquals("EC5333", result.subjectCode)
		assertEquals(3, result.credits)
		assertNull(result.gradingMode)
		assertNull(result.pensumStatus)
	}

	@Test
	fun toSubjectSearchResult_whenPensumStatusIsProvided_thenAttachesStatus() {
		val entity = SubjectCatalogCacheEntity(
			subjectCode = "EC5333",
			name = "INT. A LAS MICROONDAS",
			credits = 3,
			gradingMode = GradingMode.NUMERIC.name,
			normalizedCode = "EC5333",
			normalizedName = "INT. A LAS MICROONDAS",
			updatedAt = 123L
		)

		val result = entity.toSubjectSearchResult(
			pensumStatus = AcademicPensumNodeStatus.AVAILABLE
		)

		assertEquals(GradingMode.NUMERIC, result.gradingMode)
		assertEquals(AcademicPensumNodeStatus.AVAILABLE, result.pensumStatus)
	}

	@Test
	fun toSubjectSearchResult_whenMappedFromDetail_thenCopiesIdentityWithoutPensumStatus() {
		val detail = readySubjectDetail(subjectCode = "MAT101").detail

		val result = detail.toSubjectSearchResult()

		assertEquals("MAT101", result.subjectCode)
		assertEquals("Calculo I", result.name)
		assertEquals(5, result.credits)
		assertEquals(GradingMode.NUMERIC, result.gradingMode)
		assertNull(result.pensumStatus)
	}
}
