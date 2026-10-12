package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.classesStartLocalDate
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AcademicTermDecodingTest {
	private val termJson = """{"id":"t1","period_year":2026,"period_code":"SEP_DEC","term_kind":"current"%s}"""

	@Test
	fun createSharedJson_decodesATermWithoutClassesStartDateAsNull() {
		val term = createSharedJson().decodeFromString(AcademicTerm.serializer(), termJson.format(""))

		assertNull(term.classesStartDate)
		assertNull(term.classesStartLocalDate())
	}

	@Test
	fun createSharedJson_keepsTheClassesStartDateWhenPresent() {
		val term = createSharedJson().decodeFromString(
			AcademicTerm.serializer(),
			termJson.format(""","classes_start_date":"2026-09-28"""")
		)

		assertEquals("2026-09-28", term.classesStartDate)
		assertEquals(LocalDate(2026, 9, 28), term.classesStartLocalDate())
	}

	@Test
	fun createSharedJson_decodesAMalformedClassesStartDateWithoutFailingTheTerm() {
		val term = createSharedJson().decodeFromString(
			AcademicTerm.serializer(),
			termJson.format(""","classes_start_date":"28/09/2026"""")
		)

		assertEquals("28/09/2026", term.classesStartDate)
		assertNull(term.classesStartLocalDate())
	}
}
