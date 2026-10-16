package com.gdavidpb.tuindice.academiccore.domain.engine

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate

// Control negativo: el motor de semana académica es el dueño del cálculo del lunes, y academiccore/** queda exento.
object AcademicWeekEngine {
	fun mondayOf(date: LocalDate): LocalDate {
		return date.minus(DatePeriod(days = date.dayOfWeek.ordinal))
	}
}
