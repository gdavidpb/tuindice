package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicWeekEngine
import com.gdavidpb.tuindice.base.presentation.mapper.toShortNameText
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.summary.domain.model.SUBJECT_POINTS_TOTAL
import com.gdavidpb.tuindice.summary.domain.model.SummaryNextEvaluation
import com.gdavidpb.tuindice.summary.domain.model.SummarySubjectPoints
import com.gdavidpb.tuindice.summary.domain.model.SummaryTerm
import com.gdavidpb.tuindice.summary.presentation.model.SummarySubjectPointsItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryTermItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryTermWeekItem
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_day_date
import tuindice.summary.generated.resources.summary_evaluation_name
import tuindice.summary.generated.resources.summary_points_evaluated
import tuindice.summary.generated.resources.summary_points_no_evaluations
import tuindice.summary.generated.resources.summary_points_none
import tuindice.summary.generated.resources.summary_points_rest
import tuindice.summary.generated.resources.summary_term_next_evaluation
import tuindice.summary.generated.resources.summary_term_next_today
import tuindice.summary.generated.resources.summary_term_subjects_many
import tuindice.summary.generated.resources.summary_term_subjects_one
import tuindice.summary.generated.resources.summary_term_week_description
import tuindice.summary.generated.resources.summary_term_week_last
import tuindice.summary.generated.resources.summary_term_week_remaining_many
import tuindice.summary.generated.resources.summary_term_week_remaining_one
import tuindice.summary.generated.resources.summary_term_week_rest
import tuindice.summary.generated.resources.summary_term_week_value

/**
 * The term as the screen shows it, or null when it would have nothing under its name: no week to
 * say (outside weeks 1 to 12) and no subject whose points are known.
 */
fun SummaryTerm.toSummaryTermItemOrNull(): SummaryTermItem? {
	if (academicWeek == null && subjects.isEmpty()) return null

	return SummaryTermItem(
		nameText = UiText.Raw("${periodCode.shortLabel} $periodYear"),
		week = toSummaryTermWeekItemOrNull(),
		summaryText = toSummaryTextOrNull(),
		subjects = subjects.map { subject -> subject.toSummarySubjectPointsItem() }
	)
}

// The week and what is left of the term come together or not at all: both are counted from the week it is in.
private fun SummaryTerm.toSummaryTermWeekItemOrNull(): SummaryTermWeekItem? {
	val week = academicWeek
	val weeksLeft = weeksLeft

	return if (week == null || weeksLeft == null) {
		null
	} else {
		val totalWeeks = AcademicWeekEngine.MAX_ACADEMIC_WEEK

		SummaryTermWeekItem(
			week = week,
			totalWeeks = totalWeeks,
			valueText = UiText.Resource(Res.string.summary_term_week_value, listOf(week)),
			restText = UiText.Resource(Res.string.summary_term_week_rest, listOf(totalWeeks)),
			description = UiText.Resource(Res.string.summary_term_week_description, listOf(week, totalWeeks)),
			remainingText = weeksLeft.toRemainingText()
		)
	}
}

// "faltan 9 semanas", "falta 1 semana" and, in the twelfth, "última semana".
private fun Int.toRemainingText(): UiText {
	return when (this) {
		0 -> UiText.Resource(Res.string.summary_term_week_last)
		1 -> UiText.Resource(Res.string.summary_term_week_remaining_one)
		else -> UiText.Resource(Res.string.summary_term_week_remaining_many, listOf(this))
	}
}

// "4 materias · 16 UC": how many subjects the rows are, and the credits they add up to. Absent with no rows.
private fun SummaryTerm.toSummaryTextOrNull(): UiText? {
	val count = subjects.size
	if (count == 0) return null

	return if (count == 1) {
		UiText.Resource(Res.string.summary_term_subjects_one, listOf(count, credits))
	} else {
		UiText.Resource(Res.string.summary_term_subjects_many, listOf(count, credits))
	}
}

private fun SummarySubjectPoints.toSummarySubjectPointsItem(): SummarySubjectPointsItem {
	val graded = points?.takeIf { attemptPoints -> attemptPoints.gradedCount > 0 }

	return SummarySubjectPointsItem(
		subjectCode = subjectCode,
		valueText = graded?.earned?.formatCompactPoints(),
		restText = when {
			graded != null -> UiText.Resource(
				Res.string.summary_points_rest,
				listOf(graded.evaluated.formatCompactPoints())
			)

			points != null -> UiText.Resource(Res.string.summary_points_none)
			else -> UiText.Resource(Res.string.summary_points_no_evaluations)
		},
		attemptId = attemptId,
		evaluatedText = UiText.Resource(
			Res.string.summary_points_evaluated,
			listOf(evaluatedOfTotal.formatCompactPoints(), SUBJECT_POINTS_TOTAL.formatCompactPoints())
		),
		evaluatedFraction = (evaluatedOfTotal / SUBJECT_POINTS_TOTAL).toFloat(),
		nextText = nextEvaluation?.toNextText()
	)
}

// "Próx.: Parcial 2 · lun 26": the name of the evaluation, then its day the way the hub writes a day ("hoy" today).
private fun SummaryNextEvaluation.toNextText(): UiText {
	val name = UiText.Resource(
		Res.string.summary_evaluation_name,
		listOf(UiText.Resource(type.toLabelResource()), ordinal)
	)
	val day = if (isToday) {
		UiText.Resource(Res.string.summary_term_next_today)
	} else {
		UiText.Resource(Res.string.summary_day_date, listOf(date.dayOfWeek.toShortNameText(), date.day))
	}

	return UiText.Resource(Res.string.summary_term_next_evaluation, listOf(name, day))
}

// 41 and not 41.00, the way the Evaluations tab writes a grade that has no decimals.
internal fun Double.formatCompactPoints(): String {
	return if (this == toInt().toDouble()) formatGrade(decimals = 0) else formatGrade(decimals = 2)
}
