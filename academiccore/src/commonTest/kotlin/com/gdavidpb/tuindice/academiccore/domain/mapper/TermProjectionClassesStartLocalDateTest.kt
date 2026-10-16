package com.gdavidpb.tuindice.academiccore.domain.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.academiccore.domain.model.TermProjection
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TermProjectionClassesStartLocalDateTest {
	private fun term(classesStartDate: String?) = TermProjection(
		id = "t1",
		periodYear = 2026,
		periodCode = AcademicTermPeriod.SEP_DEC,
		termKey = "2026-SEP_DEC",
		termOrder = 20263,
		periodLabel = "SEP-DIC 2026",
		kind = TermKind.CURRENT,
		periodAverage = 0.0,
		cumulativeAverage = 0.0,
		periodCredits = 0,
		cumulativeCredits = 0,
		classesStartDate = classesStartDate
	)

	@Test
	fun classesStartLocalDate_readsTheIsoDateOfTheProjection() {
		assertEquals(LocalDate(2026, 9, 28), term("2026-09-28").classesStartLocalDate())
	}

	@Test
	fun classesStartLocalDate_isNullWhenTheProjectionHasNoneOrItIsMalformed() {
		assertNull(term(null).classesStartLocalDate())
		assertNull(term("not-a-date").classesStartLocalDate())
		assertNull(term("2026-02-31").classesStartLocalDate())
	}
}
