package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.math.max
import kotlin.math.min

/**
 * The academic weeks of a term: week 1 is the Monday-to-Sunday week the classes start in, and the
 * term is tracked through [MAX_ACADEMIC_WEEK] weeks. Works on plain dates; the caller decides which
 * day "today" is.
 */
object AcademicWeekEngine {
	const val MIN_ACADEMIC_WEEK = 1
	const val MAX_ACADEMIC_WEEK = 12
	const val DAYS_PER_WEEK = 7

	/**
	 * The Monday that opens week 1: the one of the week containing [classesStartDate] when that date
	 * is plausible for the period (see [isPlausibleClassesStartDate]), and otherwise the Monday of
	 * the week containing the 1st of the period's start month.
	 */
	fun weekOneStart(
		periodYear: Int,
		periodCode: AcademicTermPeriod,
		classesStartDate: LocalDate? = null
	): LocalDate {
		val anchor = classesStartDate
			?.takeIf { date -> isPlausibleClassesStartDate(periodYear, periodCode, date) }
			?: LocalDate(year = periodYear, month = periodCode.startMonth, day = 1)

		return mondayOf(anchor)
	}

	/**
	 * Whether [date] can be the first day of classes of the period: from the 1st of the month before
	 * the period's start month to the last day of the month the period ends in.
	 */
	internal fun isPlausibleClassesStartDate(
		periodYear: Int,
		periodCode: AcademicTermPeriod,
		date: LocalDate
	): Boolean {
		val startMonth = periodCode.startMonth
		val previousMonthFirstDay = if (startMonth == 1) {
			LocalDate(year = periodYear - 1, month = 12, day = 1)
		} else {
			LocalDate(year = periodYear, month = startMonth - 1, day = 1)
		}
		// The 1st of the month after the end month, minus one day: its last day.
		val endMonthLastDay = LocalDate(year = periodYear, month = periodCode.endMonth, day = 1)
			.plus(DatePeriod(months = 1))
			.minus(DatePeriod(days = 1))

		return date >= previousMonthFirstDay && date <= endMonthLastDay
	}

	/** The Monday of the Monday-to-Sunday week that holds [date]: [date] itself when it is a Monday. */
	fun mondayOf(date: LocalDate): LocalDate {
		return date.minus(DatePeriod(days = date.dayOfWeek.ordinal))
	}

	/** The week [date] falls in, held between [MIN_ACADEMIC_WEEK] and [MAX_ACADEMIC_WEEK]. */
	fun weekOf(weekOneStart: LocalDate, date: LocalDate): Int {
		return min(MAX_ACADEMIC_WEEK, max(MIN_ACADEMIC_WEEK, rawWeekOf(weekOneStart, date)))
	}

	/** The week [date] falls in, or null when it is before week 1 or after [MAX_ACADEMIC_WEEK]. */
	fun weekOrNull(weekOneStart: LocalDate, date: LocalDate): Int? {
		return rawWeekOf(weekOneStart, date).takeIf { week -> week in MIN_ACADEMIC_WEEK..MAX_ACADEMIC_WEEK }
	}

	// Floor division, so a date before the start lands on week 0 or below instead of rounding to 1.
	private fun rawWeekOf(weekOneStart: LocalDate, date: LocalDate): Int {
		return weekOneStart.daysUntil(date).floorDiv(DAYS_PER_WEEK) + 1
	}
}
