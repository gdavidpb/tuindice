package com.gdavidpb.tuindice.evaluations.presentation.mapper

import androidx.annotation.StringRes
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.utils.extension.capitalize
import com.gdavidpb.tuindice.base.utils.extension.daysDistance
import com.gdavidpb.tuindice.base.utils.extension.format
import com.gdavidpb.tuindice.base.utils.extension.weeksDistance
import com.gdavidpb.tuindice.evaluations.R
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

@StringRes
fun EvaluationType.stringRes() = when (this) {
	EvaluationType.TEST -> R.string.evaluation_test
	EvaluationType.ESSAY -> R.string.evaluation_essay
	EvaluationType.ATTENDANCE -> R.string.evaluation_attendance
	EvaluationType.INTERVENTIONS -> R.string.evaluation_interventions
	EvaluationType.LABORATORY -> R.string.evaluation_laboratory
	EvaluationType.MODEL -> R.string.evaluation_model
	EvaluationType.PRESENTATION -> R.string.evaluation_presentation
	EvaluationType.PROJECT -> R.string.evaluation_project
	EvaluationType.QUIZ -> R.string.evaluation_quiz
	EvaluationType.REPORT -> R.string.evaluation_report
	EvaluationType.WORKSHOP -> R.string.evaluation_workshop
	EvaluationType.WRITTEN_WORK -> R.string.evaluation_written_work
	EvaluationType.OTHER -> R.string.evaluation_other
}