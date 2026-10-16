package com.gdavidpb.tuindice.academiccore.domain.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AcademicTermClassesStartLocalDateTest {
	private fun term(classesStartDate: String?) = AcademicTerm(
		id = "t1",
		periodYear = 2026,
		periodCode = AcademicTermPeriod.SEP_DEC,
		kind = TermKind.CURRENT,
		classesStartDate = classesStartDate
	)

	@Test
	fun classesStartLocalDate_readsTheIsoDateOfTheTerm() {
		assertEquals(LocalDate(2026, 9, 28), term("2026-09-28").classesStartLocalDate())
	}

	@Test
	fun classesStartLocalDate_isNullWhenTheTermHasNoneOrItIsMalformed() {
		assertNull(term(null).classesStartLocalDate())
		assertNull(term("").classesStartLocalDate())
		assertNull(term("28/09/2026").classesStartLocalDate())
		assertNull(term("2026-02-31").classesStartLocalDate())
	}
}
