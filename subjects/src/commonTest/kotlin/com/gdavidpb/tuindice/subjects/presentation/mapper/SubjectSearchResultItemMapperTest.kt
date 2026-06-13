package com.gdavidpb.tuindice.subjects.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumNodeStatus
import com.gdavidpb.tuindice.subjects.testing.subjectSearchResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SubjectSearchResultItemMapperTest {
	@Test
	fun toSubjectSearchResultItem_whenMapped_thenUppercasesNameAndFormatsCredits() {
		val item = subjectSearchResult(subjectCode = "EC5333").toSubjectSearchResultItem()

		assertEquals("EC5333", item.subjectCode)
		assertEquals("INT. A LAS MICROONDAS Y SUS APLICACIONES", item.name)
		assertEquals("3 UC", item.creditsText)
		assertNull(item.pensumStatus)
	}

	@Test
	fun toSubjectSearchResultItem_whenPensumStatusIsKnown_thenKeepsStatus() {
		val item = subjectSearchResult(
			subjectCode = "EC5201",
			pensumStatus = AcademicPensumNodeStatus.APPROVED
		).toSubjectSearchResultItem()

		assertEquals(AcademicPensumNodeStatus.APPROVED, item.pensumStatus)
	}
}
