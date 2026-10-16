package com.gdavidpb.tuindice.academiccore.domain.mapper

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ParseClassesStartDateTest {
	@Test
	fun parseClassesStartDate_readsAnIsoDate() {
		assertEquals(LocalDate(2026, 9, 28), parseClassesStartDate("2026-09-28"))
	}

	@Test
	fun parseClassesStartDate_returnsNullWhenAbsentOrMalformed() {
		assertNull(parseClassesStartDate(null))
		assertNull(parseClassesStartDate(""))
		assertNull(parseClassesStartDate("28/09/2026"))
		assertNull(parseClassesStartDate("2026-13-40"))
	}
}
