package com.gdavidpb.tuindice.base.presentation.mapper

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalizedDateFormatterTest {
	@Test
	fun returnsSpanishMonthNames() {
		assertEquals(
			listOf(
				"enero",
				"febrero",
				"marzo",
				"abril",
				"mayo",
				"junio",
				"julio",
				"agosto",
				"septiembre",
				"octubre",
				"noviembre",
				"diciembre"
			),
			localizedFullMonthNames()
		)
	}

	@Test
	fun returnsSpanishWeekdayNames() {
		assertEquals(
			listOf("lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo"),
			localizedFullWeekdayNames()
		)
		assertEquals(
			listOf("lun", "mar", "mié", "jue", "vie", "sáb", "dom"),
			localizedShortWeekdayNames()
		)
	}

	@Test
	fun formatsMonthYearInSpanish() {
		assertEquals(
			"abril 2026",
			LocalDate(year = 2026, month = Month.APRIL, day = 1).formatLocalizedMonthYear()
		)
	}
}
