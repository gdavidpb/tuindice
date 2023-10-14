package com.gdavidpb.tuindice.evaluations.presentation.mapper

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.AreaChart
import androidx.compose.material.icons.outlined.BackHand
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CoPresent
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material.icons.outlined.HistoryEdu
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Tag
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
		daysDistance == 0L -> "Hoy"
		daysDistance == 1L -> "Mañana"
		daysDistance == -1L -> "Ayer"
		weeksDistance == 0L -> {
			val now = Date()

			if (before(now))
				"El ${format("EEEE 'pasado —' dd 'de' MMMM")}"
			else
				"Este ${format("EEEE '—' dd 'de' MMMM")}"
		}

		weeksDistance == 1L -> "El próximo ${format("EEEE '—' dd 'de' MMMM")}"
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

fun Long?.formatAsShortDayOfWeekAndDate(): String {
	return Date(this!!).format("EEE '—' dd/MM/yy")?.capitalize()!!
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

fun EvaluationType.iconRes() = when (this) {
	EvaluationType.TEST -> Icons.Outlined.FileCopy
	EvaluationType.ESSAY -> Icons.Outlined.HistoryEdu
	EvaluationType.ATTENDANCE -> Icons.Outlined.BackHand
	EvaluationType.INTERVENTIONS -> Icons.Outlined.ModeComment
	EvaluationType.LABORATORY -> Icons.Outlined.Science
	EvaluationType.MODEL -> Icons.Outlined.Apartment
	EvaluationType.PRESENTATION -> Icons.Outlined.CoPresent
	EvaluationType.PROJECT -> Icons.Outlined.AccountTree
	EvaluationType.QUIZ -> Icons.Outlined.Quiz
	EvaluationType.REPORT -> Icons.Outlined.AreaChart
	EvaluationType.WORKSHOP -> Icons.Outlined.Build
	EvaluationType.WRITTEN_WORK -> Icons.Outlined.Edit
	EvaluationType.OTHER -> Icons.Outlined.Tag
}