package com.gdavidpb.tuindice.evaluations.utils.mappers

import android.content.Context
import com.gdavidpb.tuindice.base.utils.extensions.*
import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationItem
import java.util.*

/* Presentation */

fun Evaluation.toEvaluationItem(context: Context) = EvaluationItem(
	uid = id.hashCode().toLong(),
	id = id,
	notesText = notes.ifEmpty { "─" },
	grade = grade,
	maxGrade = maxGrade,
	dateText = date.formatEvaluationDate(),
	typeText = type.formatEvaluationTypeName(context),
	date = date,
	isDone = isDone,
	data = this
)

/* Format */

fun Date.formatEvaluationDate(): String {
	val weeksLeft = weeksLeft()

	val now = Date()

	return when {
		time == 0L -> "Evaluación continua"
		isToday() -> "Hoy"
		isTomorrow() -> "Mañana"
		isYesterday() -> "Ayer"
		isThisWeek() -> {
			if (before(now))
				"El ${format("EEEE 'pasado —' dd/MM")}"
			else
				"Este ${format("EEEE '—' dd/MM")}"
		}
		isNextWeek() -> "El próximo ${format("EEEE '—' dd/MM")}"
		weeksLeft in 2..12 -> "En $weeksLeft semanas, ${format("EEEE '—' dd/MM")}"
		else -> format("EEEE '—' dd/MM/yy")?.capitalize()!!
	}
}

fun EvaluationType.formatEvaluationTypeName(context: Context) = context.getString(stringRes)