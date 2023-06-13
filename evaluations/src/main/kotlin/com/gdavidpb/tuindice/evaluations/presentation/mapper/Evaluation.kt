package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.base.utils.extension.capitalize
import com.gdavidpb.tuindice.base.utils.extension.format
import com.gdavidpb.tuindice.base.utils.extension.isNextWeek
import com.gdavidpb.tuindice.base.utils.extension.isThisWeek
import com.gdavidpb.tuindice.base.utils.extension.isToday
import com.gdavidpb.tuindice.base.utils.extension.isTomorrow
import com.gdavidpb.tuindice.base.utils.extension.isYesterday
import com.gdavidpb.tuindice.base.utils.extension.weeksLeft
import java.util.Date

fun Date.dateGroup(): String {
	val weeksLeft = weeksLeft()

	return when {
		time == 0L -> "Evaluación continua"
		isToday() -> "Hoy"
		isTomorrow() -> "Mañana"
		isYesterday() -> "Ayer"
		isThisWeek() -> {
			val now = Date()

			if (before(now))
				"El ${format("EEEE 'pasado —' dd 'de' MMMM")}"
			else
				"Este ${format("EEEE '—' dd 'de' MMMM")}"
		}

		isNextWeek() -> "El próximo ${format("EEEE '—' ddd 'de' MMMM")}"
		weeksLeft in 2..12 -> "En $weeksLeft semanas"
		else -> format("EEEE '—' dd/MM/yy")?.capitalize()!!
	}
}

fun Date.dateLabel(): String {
	return if (time == 0L)
		"Evaluación continua"
	else
		format("EEEE '—' dd/MM/yy")?.capitalize()!!
}