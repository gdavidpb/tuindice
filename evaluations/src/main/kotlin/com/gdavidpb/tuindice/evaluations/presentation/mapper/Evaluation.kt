package com.gdavidpb.tuindice.evaluations.presentation.mapper

import android.content.Context
import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.base.utils.extension.capitalize
import com.gdavidpb.tuindice.base.utils.extension.format
import com.gdavidpb.tuindice.base.utils.extension.isNextWeek
import com.gdavidpb.tuindice.base.utils.extension.isThisWeek
import com.gdavidpb.tuindice.base.utils.extension.isToday
import com.gdavidpb.tuindice.base.utils.extension.isTomorrow
import com.gdavidpb.tuindice.base.utils.extension.isYesterday
import com.gdavidpb.tuindice.base.utils.extension.weeksLeft
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationViewState
import java.util.Date

fun Evaluation.toEvaluationViewState(resourceResolver: ResourceResolver) = EvaluationViewState(
	subjectHeader = resourceResolver.getString(
		R.string.label_evaluation_plan_header,
		subjectCode,
		subjectName
	),
	name = name,
	maxGrade = maxGrade,
	date = date,
	isDateSet = (date.time != 0L),
	type = type
)

fun Evaluation.toEvaluationItem(context: Context) = EvaluationItem(
	uid = evaluationId.hashCode().toLong(),
	id = evaluationId,
	nameText = name.ifEmpty { "─" },
	grade = grade,
	maxGrade = maxGrade,
	dateText = date.formatEvaluationDate(),
	typeText = type.formatEvaluationTypeName(context),
	date = date,
	isDone = isDone,
	data = this
)

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