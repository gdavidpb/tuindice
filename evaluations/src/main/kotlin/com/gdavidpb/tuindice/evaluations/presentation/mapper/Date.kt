package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.mapper.daysToNow
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.presentation.mapper.weeksToNow
import com.gdavidpb.tuindice.base.utils.extension.capitalize

fun Long.isDatePassed() = this != 0L && daysToNow() < 0

fun Long.formatAsToNow(): String {
	val daysDistance = daysToNow()
	val weeksDistance = weeksToNow()

	return when {
		this == 0L -> "Evaluación continua"
		daysDistance == 0 -> "Hoy"
		daysDistance == 1 -> "Mañana"
		daysDistance == -1 -> "Ayer"
		weeksDistance == 0 -> {
			if (this < System.currentTimeMillis())
				"El ${formatDate("EEEE 'pasado —' dd 'de' MMMM")}"
			else
				"Este ${formatDate("EEEE '—' dd 'de' MMMM")}"
		}

		weeksDistance == 1 -> "El próximo ${formatDate("EEEE '—' dd 'de' MMMM")}"
		weeksDistance in 2..12 -> "En $weeksDistance semanas"
		else -> formatDate("EEEE '—' dd/MM/yy")?.capitalize()!!
	}
}

fun Long.formatAsDayOfWeekAndDate(): String {
	return if (this == 0L)
		"Evaluación continua"
	else
		formatDate("EEEE '—' dd/MM/yy")?.capitalize()!!
}

fun Long.formatAsShortDayOfWeekAndDate(): String {
	return formatDate("EEE '—' dd/MM/yy")?.capitalize()!!
}