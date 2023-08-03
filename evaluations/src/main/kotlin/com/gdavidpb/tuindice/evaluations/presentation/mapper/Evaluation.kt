package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.base.utils.extension.capitalize
import com.gdavidpb.tuindice.base.utils.extension.daysDistance
import com.gdavidpb.tuindice.base.utils.extension.format
import com.gdavidpb.tuindice.base.utils.extension.weeksDistance
import java.util.Date

fun Date.formatAsToNow(): String {
	val daysDistance = daysDistance()
	val weeksDistance = weeksDistance()

	return when {
		time == 0L -> "Evaluación continua"
		daysDistance == 0 -> "Hoy"
		daysDistance == 1 -> "Mañana"
		daysDistance == -1 -> "Ayer"
		weeksDistance == 0 -> {
			val now = Date()

			if (before(now))
				"El ${format("EEEE 'pasado —' dd 'de' MMMM")}"
			else
				"Este ${format("EEEE '—' dd 'de' MMMM")}"
		}

		weeksDistance == 1 -> "El próximo ${format("EEEE '—' dd 'de' MMMM")}"
		weeksDistance in 2..12 -> "En $weeksDistance semanas"
		else -> format("EEEE '—' dd/MM/yy")?.capitalize()!!
	}
}

fun Date.formatAsDayOfWeekAndDate(): String {
	return if (time == 0L)
		"Evaluación continua"
	else
		format("EEEE '—' dd/MM/yy")?.capitalize()!!
}