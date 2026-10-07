@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.record.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private const val AcademicCalendarTimeZoneId = "America/Caracas"

/**
 * The order (`year * 10 + period sequence`) of the academic term this clock says it is, in the
 * university's time zone. The term options the person can plan and the check that a planned term is
 * not in the past both read it, so they must read the same clock.
 */
internal fun Clock.currentAcademicTermOrder(): Int {
	val dateTime = now().toLocalDateTime(TimeZone.of(AcademicCalendarTimeZoneId))

	return dateTime.year * 10 + periodForMonth(dateTime.month.ordinal + 1).sequence
}

private fun periodForMonth(month: Int): AcademicTermPeriod {
	return when (month) {
		in 1..3 -> AcademicTermPeriod.JAN_MAR
		in 4..6 -> AcademicTermPeriod.APR_JUL
		in 7..8 -> AcademicTermPeriod.JUL_AUG
		else -> AcademicTermPeriod.SEP_DEC
	}
}
