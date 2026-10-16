package com.gdavidpb.tuindice.evaluations.data.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicTermEntity
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AcademicTermMappingTest {
	private fun entity(classesStartDate: String?) = AcademicTermEntity(
		id = "term-1",
		periodYear = 2026,
		periodCode = AcademicTermPeriod.SEP_DEC.name,
		termKey = "2026-SEP_DEC",
		termOrder = 20266,
		periodLabel = "Septiembre - Diciembre 2026",
		kind = "CURRENT",
		classesStartDate = classesStartDate
	)

	// The only link between Room and the anchor of the weeks: leaving the date out compiles and
	// would send every week back to the 1st of the month without failing anything else.
	@Test
	fun toLocalCurrentTermDescriptor_carriesTheFirstDayOfClasses() {
		val descriptor = entity(classesStartDate = "2026-09-28").toLocalCurrentTermDescriptor()

		assertEquals(LocalDate(2026, 9, 28), descriptor.classesStartDate)
		assertEquals(LocalDate(2026, 9, 28), descriptor.toEvaluationTermDescriptor().classesStartDate)
	}

	@Test
	fun toLocalCurrentTermDescriptor_withoutAValidDate_hasNone() {
		assertNull(entity(classesStartDate = null).toLocalCurrentTermDescriptor().classesStartDate)
		assertNull(entity(classesStartDate = "28/09/2026").toLocalCurrentTermDescriptor().classesStartDate)
	}

	@Test
	fun toLocalCurrentTermDescriptor_carriesThePeriodOfTheTerm() {
		val descriptor = entity(classesStartDate = null).toLocalCurrentTermDescriptor()

		assertEquals("term-1", descriptor.id)
		assertEquals(2026, descriptor.periodYear)
		assertEquals(AcademicTermPeriod.SEP_DEC, descriptor.periodCode)
		assertEquals("Septiembre - Diciembre 2026", descriptor.periodLabel)
	}
}
