package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.mapper.DateTextStyle
import com.gdavidpb.tuindice.base.presentation.mapper.daysToNow
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.presentation.mapper.weeksToNow
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.base.utils.extension.capitalize

fun Long?.isDatePassed() = this != null && daysToNow() < 0

fun Long?.formatAsToNow(): String {
	if (this == null) return "Evaluación continua"

	val daysDistance = daysToNow()
	val weeksDistance = weeksToNow()

	return when {
		daysDistance == 0 -> "Hoy"
		daysDistance == 1 -> "Mañana"
		daysDistance == -1 -> "Ayer"
		weeksDistance == 0L -> {
			if (this < currentTimeMillis())
				"El ${formatDate(DateTextStyle.WEEKDAY_PAST_DAY_MONTH)}"
			else
				"Este ${formatDate(DateTextStyle.WEEKDAY_DAY_MONTH)}"
		}

		weeksDistance == 1L -> "El próximo ${formatDate(DateTextStyle.WEEKDAY_DAY_MONTH)}"
		weeksDistance in 2..12 -> "En $weeksDistance semanas"
		else -> formatDate(DateTextStyle.WEEKDAY_NUMERIC_DATE)?.capitalize()!!
	}
}

fun Long?.formatAsDayOfWeekAndDate(): String {
	if (this == null) return "Evaluación continua"

	return formatDate(DateTextStyle.WEEKDAY_NUMERIC_DATE)?.capitalize()!!
}

fun Long.formatAsShortDayOfWeekAndDate(): String {
	return formatDate(DateTextStyle.SHORT_WEEKDAY_NUMERIC_DATE)?.capitalize()!!
}
